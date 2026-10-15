package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateColossus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenBlast.class, ChandraNalaar.class, GrizzlyBears.class, Millstone.class, SeaGateColossus.class})
class MoltenBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 2 damage to a creature")
    void damageModeDealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Damage mode deals 2 damage to a planeswalker")
    void damageModeDealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        cast(0, planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroy mode destroys a target artifact")
    void destroyModeDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        cast(1, artifact);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Each mode rejects the other mode's target")
    void modesRejectIllegalTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.setHand(player1, List.of(new MoltenBlast()));
        addMana();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new MoltenBlast()));
        addMana();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage mode marks exactly 2 damage without destroying a larger artifact creature")
    void damageModeDoesNotDestroyArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SeaGateColossus());

        cast(0, creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player2, "Sea Gate Colossus");
    }

    @Test
    @DisplayName("Destroy mode destroys an artifact creature regardless of toughness")
    void destroyModeDestroysArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SeaGateColossus());

        cast(1, creature);

        harness.assertNotOnBattlefield(player2, "Sea Gate Colossus");
        harness.assertInGraveyard(player2, "Sea Gate Colossus");
    }

    @Test
    @DisplayName("Damage mode cannot target a player")
    void damageModeRejectsPlayer() {
        harness.setHand(player1, List.of(new MoltenBlast()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode can target the caster's own artifact")
    void destroyModeCanTargetOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        cast(1, artifact);

        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("A target that leaves and returns is not destroyed by the pending spell")
    void returningArtifactIsANewTarget() {
        Millstone card = new Millstone();
        Permanent original = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new MoltenBlast()));
        addMana();
        harness.castModalInstant(player1, 0, 1, List.of(original.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, card);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returned);
        harness.assertNotInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player1, "Molten Blast");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new MoltenBlast()));
        addMana();
        harness.castModalInstant(player1, 0, mode, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
