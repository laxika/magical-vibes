package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfSinewAndSteel.class, GrizzlyBears.class, ChandraNalaar.class, MindStone.class})
class SwordOfSinewAndSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from black and red")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage trigger destroys up to one planeswalker and up to one artifact")
    void combatDamageDestroysPlaneswalkerAndArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(planeswalker, artifact);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(planeswalker.getCard(), artifact.getCard());
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = new Permanent(new SwordOfSinewAndSteel());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        ChandraNalaar card = new ChandraNalaar();
        card.setLoyalty(loyalty);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
