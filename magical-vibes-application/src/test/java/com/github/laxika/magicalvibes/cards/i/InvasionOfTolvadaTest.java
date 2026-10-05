package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RealmbreakersGrasp;
import com.github.laxika.magicalvibes.cards.t.TheBrokenSky;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopperHostCrusher.class, InvasionOfTolvada.class, Plains.class,
        RealmbreakersGrasp.class, VolcanicSpite.class, TheBrokenSky.class})
class InvasionOfTolvadaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted nonbattle permanent card from the graveyard")
    void etbReturnsNonbattlePermanent() {
        CopperHostCrusher creature = new CopperHostCrusher();
        InvasionOfTolvada battle = new InvasionOfTolvada();
        VolcanicSpite instant = new VolcanicSpite();
        harness.setGraveyard(player1, List.of(creature, battle, instant));

        castInvasion();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Copper Host Crusher");
        harness.assertInGraveyard(player1, "Invasion of Tolvada");
        harness.assertInGraveyard(player1, "Volcanic Spite");
    }

    @Test
    @DisplayName("Defeating the Siege casts The Broken Sky transformed")
    void defeatCastsBackFace() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfTolvada());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent brokenSky = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TheBrokenSky)
                .findFirst()
                .orElseThrow();
        assertThat(brokenSky.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The Broken Sky boosts token creatures and creates a lifelink Spirit at your end step")
    void backFaceBoostsTokensAndCreatesSpirit() {
        Permanent brokenSky = harness.addToBattlefieldAndReturn(player1, new InvasionOfTolvada());
        brokenSky.setCard(brokenSky.getOriginalCard().getBackFaceCard());
        brokenSky.setTransformed(true);
        Permanent nontoken = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());

        assertThat(gqs.getEffectivePower(gd, nontoken)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, nontoken, Keyword.LIFELINK)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Spirit".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The Siege can resolve without any legal graveyard targets")
    void siegeResolvesWithNoLegalTargets() {
        harness.setGraveyard(player1, List.of(new InvasionOfTolvada(), new VolcanicSpite()));

        castInvasion();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Invasion of Tolvada");
        harness.assertInGraveyard(player1, "Volcanic Spite");
    }

    @Test
    @DisplayName("ETB can return a land and cannot target an opponent's graveyard")
    void returnsLandFromOwnGraveyard() {
        Plains ownLand = new Plains();
        Plains opponentsLand = new Plains();
        harness.setGraveyard(player1, List.of(ownLand));
        harness.setGraveyard(player2, List.of(opponentsLand));

        castInvasion();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownLand.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("ETB does not return a target removed from the graveyard before resolution")
    void removedTargetIsNotReturned() {
        Plains land = new Plains();
        harness.setGraveyard(player1, List.of(land));
        castInvasion();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(land));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Invasion of Tolvada");
    }

    @Test
    @DisplayName("An Aura returned by the ETB enters attached to a chosen legal permanent")
    void returnsAuraAttachedToChosenPermanent() {
        RealmbreakersGrasp aura = new RealmbreakersGrasp();
        Permanent host = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setGraveyard(player1, List.of(aura));
        castInvasion();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, host.getId());

        harness.assertOnBattlefield(player1, "Realmbreaker's Grasp");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(aura.getId()))
                .findFirst().orElseThrow().getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    @DisplayName("An Aura with no legal attachment stays in the graveyard")
    void auraWithoutLegalAttachmentStaysInGraveyard() {
        RealmbreakersGrasp aura = new RealmbreakersGrasp();
        harness.setGraveyard(player1, List.of(aura));
        castInvasion();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Realmbreaker's Grasp");
        harness.assertNotOnBattlefield(player1, "Realmbreaker's Grasp");
    }

    @Test
    @DisplayName("The Broken Sky does not trigger during an opponent's end step")
    void noSpiritAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new TheBrokenSky());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("The Broken Sky only boosts creature tokens its controller controls")
    void onlyOwnCreatureTokensAreBoosted() {
        harness.addToBattlefield(player1, new TheBrokenSky());
        CopperHostCrusher ownTokenCard = new CopperHostCrusher();
        ownTokenCard.setToken(true);
        CopperHostCrusher opponentTokenCard = new CopperHostCrusher();
        opponentTokenCard.setToken(true);
        Plains landTokenCard = new Plains();
        landTokenCard.setToken(true);
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, ownTokenCard);
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2, opponentTokenCard);
        Permanent landToken = harness.addToBattlefieldAndReturn(player1, landTokenCard);

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, landToken, Keyword.LIFELINK)).isFalse();
    }

    private void castInvasion() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new InvasionOfTolvada()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
    }
}
