package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shuriken.class, GnarledMass.class, AngelicCurator.class})
class ShurikenTest extends BaseCardTest {

    @Test
    @DisplayName("Shuriken deals 2 damage and its target's controller gains it")
    void damagesCreatureAndChangesControl() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shuriken);
    }

    @Test
    @DisplayName("Shuriken stays with its controller when unattached from a Ninja")
    void doesNotChangeControlWhenUnattachedFromNinja() {
        Permanent ninja = addCreatureReady(player1, new GnarledMass());
        TestCards.mutableCard(ninja).setSubtypes(List.of(CardSubtype.NINJA));
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(ninja.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shuriken);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(shuriken);
    }

    @Test
    @DisplayName("Activating Shuriken taps the equipped creature")
    void activationTapsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Shuriken to a creature you control")
    void equipAttachesToCreature() {
        Permanent shuriken = addShurikenReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shuriken.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An unequipped creature does not gain Shuriken's activated ability")
    void unequippedCreatureDoesNotGainAbility() {
        addShurikenReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from artifacts prevents Shuriken's damage but not the rest of its ability")
    void protectionFromArtifactsPreventsDamage() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new AngelicCurator());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken);
    }

    private Permanent addShurikenReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Shuriken());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
