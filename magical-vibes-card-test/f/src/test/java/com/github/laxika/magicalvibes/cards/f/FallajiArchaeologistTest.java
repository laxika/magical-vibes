package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.p.PrisonSentence;
import com.github.laxika.magicalvibes.cards.s.ScatterRay;
import com.github.laxika.magicalvibes.cards.t.TocasiasDigSite;
import com.github.laxika.magicalvibes.cards.t.TocasiasOnulet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallajiArchaeologist.class, ScatterRay.class, TocasiasDigSite.class, ArgothianSprite.class,
        EnergyRefractor.class, PrisonSentence.class, TocasiasOnulet.class, GoForTheThroat.class})
class FallajiArchaeologistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled noncreature, nonland card for the hand")
    void acceptsMilledNoncreatureNonlandCard() {
        ScatterRay spell = new ScatterRay();
        setLibrary(spell, new TocasiasDigSite(), new ArgothianSprite());

        Permanent archaeologist = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Tocasia's Dig Site", "Argothian Sprite");
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the eligible milled card puts a +1/+1 counter on Fallaji Archaeologist")
    void declinesEligibleCardAndGetsCounter() {
        ScatterRay spell = new ScatterRay();
        setLibrary(spell, new TocasiasDigSite(), new ArgothianSprite());

        Permanent archaeologist = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature and land cards are not eligible and the counter is automatic")
    void noEligibleCardGetsCounter() {
        setLibrary(new TocasiasDigSite(), new ArgothianSprite(), new TocasiasDigSite());

        Permanent archaeologist = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Accepting one eligible card leaves the other eligible cards in the graveyard")
    void returnsOnlyOneEligibleCard() {
        ScatterRay spell = new ScatterRay();
        EnergyRefractor artifact = new EnergyRefractor();
        PrisonSentence enchantment = new PrisonSentence();
        setLibrary(spell, artifact, enchantment);

        Permanent archaeologist = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact, enchantment);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the first offer still allows returning a noncreature artifact")
    void returnsSecondEligibleCard() {
        ScatterRay spell = new ScatterRay();
        EnergyRefractor artifact = new EnergyRefractor();
        TocasiasDigSite land = new TocasiasDigSite();
        setLibrary(spell, artifact, land);

        Permanent archaeologist = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A milled enchantment can be returned after declining the other cards")
    void returnsMilledEnchantment() {
        ScatterRay spell = new ScatterRay();
        EnergyRefractor artifact = new EnergyRefractor();
        PrisonSentence enchantment = new PrisonSentence();
        setLibrary(spell, artifact, enchantment);

        Permanent archaeologist = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter is added once and only after every eligible offer is declined")
    void declinesAllEligibleCardsBeforeCounter() {
        setLibrary(new ScatterRay(), new EnergyRefractor(), new PrisonSentence());

        Permanent archaeologist = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifact creatures cannot be returned by the ability")
    void artifactCreatureIsNotEligible() {
        TocasiasOnulet creature = new TocasiasOnulet();
        setLibrary(creature, new ArgothianSprite(), new TocasiasDigSite());

        Permanent archaeologist = castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the top three cards are milled and older graveyard cards are ineligible")
    void onlyCardsMilledThisWayAreEligible() {
        ScatterRay oldSpell = new ScatterRay();
        EnergyRefractor fourthCard = new EnergyRefractor();
        harness.setGraveyard(player1, List.of(oldSpell));
        setLibrary(new TocasiasDigSite(), new ArgothianSprite(), new TocasiasOnulet(), fourthCard);

        Permanent archaeologist = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldSpell).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A short library is milled completely and still allows returning a card")
    void shortLibraryStillReturnsCard() {
        EnergyRefractor artifact = new EnergyRefractor();
        setLibrary(artifact);

        Permanent archaeologist = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An empty library still gives a counter")
    void emptyLibraryGetsCounter() {
        setLibrary();

        Permanent archaeologist = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger still mills and returns a card after the Archaeologist is destroyed")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        ScatterRay spell = new ScatterRay();
        TocasiasDigSite land = new TocasiasDigSite();
        ArgothianSprite creature = new ArgothianSprite();
        setLibrary(spell, land, creature);
        harness.castFromHand(player1, new FallajiArchaeologist(), "{1}{U}");
        harness.passBothPriorities();
        Permanent archaeologist = findPermanent(player1, "Fallaji Archaeologist");
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, archaeologist.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fallaji Archaeologist");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(archaeologist.getCard(), land, creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new FallajiArchaeologist(), "{1}{U}");
        resolveAllTriggers();
        return findPermanent(player1, "Fallaji Archaeologist");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
