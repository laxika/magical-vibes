package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfAmity.class, HolyStrength.class, GrizzlyBears.class})
class HeraldOfAmityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only an Aura among the top eight cards")
    void entersAndOffersOnlyAuraFromTopEight() {
        HolyStrength aura = new HolyStrength();
        List<Card> library = List.of(
                aura,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castHerald(library, 4);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("Casts the chosen Aura for free and puts the rest on the library bottom")
    void castsAuraForFreeAndBottomsTheRest() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        List<Card> library = List.of(
                aura,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castHerald(library, 5);

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(1, library.size()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == aura
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Puts all eight cards on the library bottom when no Aura is found")
    void noAuraFinishesWithoutCastChoice() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castHerald(library, 4);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("Attack boost counts only Auras controlled by Herald's controller and expires")
    void attackBoostCountsControlledAurasAndExpires() {
        Permanent herald = addCreatureReady(player1, new HeraldOfAmity());
        addAttachedAura(player1, herald);

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addAttachedAura(player2, opponentCreature);
        addAttachedAura(player2, opponentCreature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(herald)));
        resolveAllTriggers();

        assertThat(herald.getPowerModifier()).isEqualTo(1);
        assertThat(herald.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(herald.getPowerModifier()).isZero();
        assertThat(herald.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Choosing one of multiple Auras returns the unchosen Aura to the library")
    void castsOnlyOneAura() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength chosen = new HolyStrength();
        HolyStrength unchosen = new HolyStrength();
        castHerald(List.of(chosen, unchosen), 4);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == chosen
                        && target.getId().equals(permanent.getAttachedTo()))
                .noneMatch(permanent -> permanent.getCard() == unchosen);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Declining the Aura cast returns a short library in full")
    void mayDeclineAuraWithShortLibrary() {
        HolyStrength aura = new HolyStrength();
        List<Card> library = List.of(aura, new GrizzlyBears());
        castHerald(library, 4);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == aura);
    }

    @Test
    @DisplayName("Only the top eight cards are exiled and returned below the untouched library")
    void preservesCardsBelowTopEight() {
        HolyStrength ninthCard = new HolyStrength();
        Card tenthCard = new GrizzlyBears();
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                ninthCard, tenthCard);
        castHerald(library, 4);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        List<Card> remainingLibrary = gd.playerDecks.get(player1.getId());
        assertThat(remainingLibrary).hasSize(10);
        assertThat(remainingLibrary.subList(0, 2)).containsExactly(ninthCard, tenthCard);
        assertThat(remainingLibrary.subList(2, 10))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 8));
    }

    @Test
    @DisplayName("Attack boost counts Auras at resolution and retains that amount afterward")
    void attackBoostUsesResolutionCount() {
        Permanent herald = addCreatureReady(player1, new HeraldOfAmity());
        Permanent firstAura = addAttachedAura(player1, herald);
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isNotEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);
        addAttachedAura(player1, herald);
        addAttachedAura(player1, herald);
        resolveAllTriggers();

        assertThat(herald.getPowerModifier()).isEqualTo(2);
        assertThat(herald.getToughnessModifier()).isEqualTo(2);
        addAttachedAura(player1, herald);
        assertThat(herald.getPowerModifier()).isEqualTo(2);
        assertThat(herald.getToughnessModifier()).isEqualTo(2);
    }

    private void castHerald(List<Card> library, int whiteMana) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new HeraldOfAmity()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private Permanent addAttachedAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
