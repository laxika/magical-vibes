package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrashOfRhinos;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fiendlash.class, CrashOfRhinos.class, Shock.class})
class FiendlashTest extends BaseCardTest {

    @Test
    @DisplayName("Fiendlash gives the equipped creature +2/+0 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("When the equipped creature is dealt damage, it deals its power to a player")
    void damagedEquippedCreatureDealsItsPowerToTargetPlayer() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Fiendlash's trigger cannot target a creature")
    void triggerCannotTargetCreature() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());
        Permanent otherCreature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player2.getId()).doesNotContain(otherCreature.getId());
    }

    private Permanent addLashReady() {
        Permanent lash = new Permanent(new Fiendlash());
        lash.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lash);
        return lash;
    }
}
