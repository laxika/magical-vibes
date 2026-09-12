package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.f.Flicker;
import com.github.laxika.magicalvibes.cards.p.PlowUnder;
import com.github.laxika.magicalvibes.cards.s.ScentOfCinder;
import com.github.laxika.magicalvibes.cards.s.ScentOfNightshade;
import com.github.laxika.magicalvibes.cards.s.SigilOfSleep;
import com.github.laxika.magicalvibes.cards.t.ThranFoundry;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        RayneAcademyChancellor.class,
        BraidwoodCup.class,
        Flicker.class,
        PlowUnder.class,
        ScentOfCinder.class,
        ScentOfNightshade.class,
        SigilOfSleep.class,
        ThranFoundry.class,
        YavimayaHollow.class
})
class RayneAcademyChancellorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when an opponent's spell targets you")
    void drawsWhenOpponentTargetsYou() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new ScentOfCinder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when an opponent's spell targets a noncreature permanent you control")
    void drawsWhenOpponentTargetsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        Permanent cup = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Flicker()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, cup.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws an additional card when enchanted")
    void drawsAdditionalCardWhenEnchanted() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        UUID rayneId = harness.getPermanentId(player1, "Rayne, Academy Chancellor");

        harness.setHand(player1, List.of(new SigilOfSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, rayneId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new ScentOfNightshade()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, rayneId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger for your own spell")
    void doesNotTriggerForOwnSpell() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        UUID rayneId = harness.getPermanentId(player1, "Rayne, Academy Chancellor");
        harness.setHand(player1, List.of(new ScentOfNightshade()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, rayneId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an opponent's ability targets you")
    void drawsWhenOpponentAbilityTargetsYou() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addToBattlefield(player2, new ThranFoundry());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can decline the optional draw")
    void canDeclineOptionalDraw() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new ScentOfNightshade()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Rayne, Academy Chancellor"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Can decline the additional draw while enchanted")
    void canDeclineAdditionalDrawWhileEnchanted() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        UUID rayneId = harness.getPermanentId(player1, "Rayne, Academy Chancellor");

        harness.setHand(player1, List.of(new SigilOfSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, rayneId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new ScentOfNightshade()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, rayneId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Checks whether Rayne is enchanted when the trigger resolves")
    void checksEnchantmentAtTriggerResolution() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        UUID rayneId = harness.getPermanentId(player1, "Rayne, Academy Chancellor");
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfSleep());
        sigil.setAttachedTo(rayneId);
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new ScentOfNightshade()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, rayneId);
        gd.playerBattlefields.get(player1.getId()).remove(sigil);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Triggers once for each of two lands targeted by one opponent spell")
    void triggersOnceForEachQualifyingTarget() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());

        harness.setHand(player2, List.of(new PlowUnder()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, List.of(firstLand.getId(), secondLand.getId()));

        assertThat(gd.stack).hasSize(3);
    }
}
