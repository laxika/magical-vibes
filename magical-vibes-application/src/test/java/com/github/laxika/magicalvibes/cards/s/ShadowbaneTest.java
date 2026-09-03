package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.k.KaerveksPurge;
import com.github.laxika.magicalvibes.cards.t.TalruumMinotaur;
import com.github.laxika.magicalvibes.cards.u.UrborgPanther;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        Shadowbane.class,
        UrborgPanther.class,
        TalruumMinotaur.class,
        FemerefArchers.class,
        Incinerate.class,
        KaerveksPurge.class
})
class ShadowbaneTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shadowbane prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castShadowbane(player1);
        addCreatureReady(player2, new UrborgPanther());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the chosen black source's damage to you and gains that much life")
    void preventsDamageToControllerAndGainsLifeFromBlackSource() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent panther = addCreatureReady(player2, new UrborgPanther());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, panther.getId());

        panther.setAttacking(true);
        resolveCombat(player2);

        // 2 damage prevented, 2 life gained because Urborg Panther is black
        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents a non-black chosen source's damage but grants no life")
    void preventsDamageWithoutLifeGainFromNonBlackSource() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent minotaur = addCreatureReady(player2, new TalruumMinotaur());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, minotaur.getId());

        minotaur.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the chosen source's damage to a creature you control")
    void preventsDamageToControlledCreature() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent panther = addCreatureReady(player2, new UrborgPanther());
        Permanent archers = addCreatureReady(player1, new FemerefArchers());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, panther.getId());

        panther.setAttacking(true);
        archers.setBlocking(true);
        archers.addBlockingTarget(0);
        resolveCombat(player2);

        // All 2 damage Urborg Panther assigns to the blocker is prevented, so the 2/2 survives
        // and its controller gains 2 life because Urborg Panther is black.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Femeref Archers"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent chosen = addCreatureReady(player2, new UrborgPanther());
        Permanent other = addCreatureReady(player2, new TalruumMinotaur());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castShadowbane(player1);
        Permanent panther = addCreatureReady(player2, new UrborgPanther());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, panther.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell to a creature you control")
    void preventsDamageFromChosenSpellToControlledCreature() {
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Incinerate incinerate = new Incinerate();

        harness.setHand(player1, List.of(incinerate, new Shadowbane()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, archers.getId());
        StackEntry incinerateEntry = gd.stack.getLast();
        harness.castInstant(player1, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, incinerateEntry.getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(archers);
        assertThat(archers.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gains life when a chosen black spell's damage is prevented")
    void gainsLifeWhenChosenBlackSpellDealsDamage() {
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        KaerveksPurge purge = new KaerveksPurge();
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(purge, new Shadowbane()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 3, archers.getId());
        StackEntry purgeEntry = gd.stack.getLast();
        harness.castInstant(player1, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, purgeEntry.getCard().getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    private void castShadowbane(Player player) {
        harness.setHand(player, List.of(new Shadowbane()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castInstant(player, 0);
    }

}
