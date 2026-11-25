package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.a.AquaticSubtlety;
import com.github.laxika.magicalvibes.cards.c.CraterousStomp;
import com.github.laxika.magicalvibes.cards.c.CircadianStruggle;
import com.github.laxika.magicalvibes.cards.e.ElvishElegy;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.s.Sear;
import com.github.laxika.magicalvibes.cards.y.ThoughtweftsCall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LimitlessRekindling.class, AquaticSubtlety.class, CraterousStomp.class,
        CircadianStruggle.class, ElvishElegy.class, RiteOfFlame.class, ThoughtweftsCall.class, ThayanEvokers.class,
        Sear.class})
class LimitlessRekindlingTest extends BaseCardTest {

    /** The ECL expansion's instant and sorcery cards. */
    private static final List<String> ECL_INSTANTS_AND_SORCERIES = List.of(
            "Appeal to Eirdu", "Crib Swap", "Keep Out", "Morningtide's Light", "Personify", "Protective Response",
            "Pyrrhic Strike", "Riverguard's Reflexes", "Winnowing", "Glen Elendra's Answer",
            "Harmonized Crescendo", "Mirrorform", "Rime Chill", "Run Away Together", "Spell Snare", "Swat Away",
            "Temporal Cleansing", "Thirst for Identity", "Unexpected Assistance", "Wanderwine Farewell",
            "Wild Unraveling", "Auntie's Sentence", "Blight Rot", "Bloodline Bidding", "Bogslither's Embrace",
            "Darkness Descends", "Dose of Dawnglow", "Nameless Inversion", "Perfect Intimidation", "Requiting Hex",
            "Scarblade's Malice", "Unbury", "Boulder Dash", "Burning Curiosity", "Cinder Strike",
            "End-Blaze Epiphany", "Feed the Flames", "Giantfall", "Goatnap", "Impolite Entrance",
            "Kindle the Inner Flame", "Reckless Ransacking", "Sear", "Soul Immolation", "Tweeze",
            "Assert Perfection", "Blossoming Defense", "Celestial Reunion", "Midnight Tilling", "Spry and Mighty",
            "Tend the Sprigs", "Thoughtweft Charge", "Unforgiving Aim", "Ashling's Command", "Brigid's Command",
            "Dream Harvest", "Grub's Command", "Sygg's Command", "Trystan's Command");


    @Test
    void conjuresRandomInstantOrSorceryIntoExileForFreeCastUntilEndOfTurn() {
        harness.setHand(player1, java.util.List.of(new LimitlessRekindling()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        Card exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        assertThat(exiled.getName()).isIn(ECL_INSTANTS_AND_SORCERIES);
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiled.getId(), player1.getId());
        assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN)).contains(exiled.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).contains(exiled.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(exiled.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).doesNotContain(exiled.getId());
    }

    @Test
    void stormConjuresOneAdditionalCardForEachEarlierSpellWithoutCastingTheCopy() {
        harness.setHand(player1, java.util.List.of(new RiteOfFlame(), new LimitlessRekindling()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
        for (Card exiled : gd.getPlayerExiledCards(player1.getId())) {
            assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiled.getId(), player1.getId());
            assertThat(gd.exilePlayWithoutPayingCardIds()).contains(exiled.getId());
            assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN)).contains(exiled.getId());
        }
    }

    @Test
    void conjuringIntoExileTriggersConjureAbilities() {
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.setHand(player1, java.util.List.of(new LimitlessRekindling()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Conjures a random instant or sorcery card of the ECL expansion, as its text says")
    void conjuresAnInstantOrSorceryOfTheEclExpansion() {
        harness.setHand(player1, List.of(new LimitlessRekindling()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        Card exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        assertThat(exiled.getName()).isIn(ECL_INSTANTS_AND_SORCERIES);
        assertThat(exiled.hasType(CardType.INSTANT) || exiled.hasType(CardType.SORCERY)).isTrue();
    }
}
