package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.v.VisionSkeins;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dovescape.class, Demonfire.class, DovinsVeto.class, MistralCharger.class, VisionSkeins.class})
class DovescapeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and gives its caster Birds equal to its mana value")
    void countersNoncreatureSpellAndCreatesBirdsForCaster() {
        harness.addToBattlefield(player1, new Dovescape());
        harness.castFromHand(player2, new VisionSkeins(), "{1}{U}");

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Bird")).hasSize(2);
        assertThat(findPermanents(player2, "Bird")).allSatisfy(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(bird.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(bird.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
            assertThat(bird.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
            assertThat(bird.getCard().hasKeyword(Keyword.FLYING)).isTrue();
            assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
        });
        assertThat(findPermanents(player1, "Bird")).isEmpty();
        harness.assertInGraveyard(player2, "Vision Skeins");
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new Dovescape());
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
        harness.assertOnBattlefield(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("Creates Birds and lets an uncounterable spell resolve")
    void createsBirdsWhenSpellCannotBeCountered() {
        harness.addToBattlefield(player1, new Dovescape());
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(findPermanents(player2, "Bird")).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Demonfire");
    }

    @Test
    @DisplayName("Creates Birds when another effect has already countered the triggering spell")
    void createsBirdsWhenTriggeringSpellHasLeftTheStack() {
        harness.addToBattlefield(player1, new Dovescape());
        VisionSkeins visionSkeins = new VisionSkeins();
        DovinsVeto dovinsVeto = new DovinsVeto();
        harness.setHand(player1, List.of(visionSkeins));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(dovinsVeto));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, visionSkeins.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(findPermanents(player2, "Bird")).hasSize(2);
        harness.assertInGraveyard(player1, "Vision Skeins");
        harness.assertInGraveyard(player2, "Dovin's Veto");
    }
    @Test
    @DisplayName("Each Dovescape creates Birds even after the first trigger counters the spell")
    void multipleDovescapesCreateBirdsForTheSameCaster() {
        harness.addToBattlefield(player1, new Dovescape());
        harness.addToBattlefield(player2, new Dovescape());
        harness.castFromHand(player1, new VisionSkeins(), "{1}{U}");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(4);
        assertThat(findPermanents(player2, "Bird")).isEmpty();
        harness.assertInGraveyard(player1, "Vision Skeins");
    }

    @Test
    @DisplayName("Includes the chosen X in mana value when countering an X spell")
    void counteredXSpellCreatesBirdsIncludingChosenX() {
        harness.addToBattlefield(player1, new Dovescape());
        harness.setHand(player1, List.of(new Demonfire(), new MistralCharger()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 4, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(5);
        assertThat(findPermanents(player2, "Bird")).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Demonfire");
    }

    @Test
    @DisplayName("An X spell cast with X zero creates Birds for its fixed mana cost")
    void zeroXStillCountsTheFixedManaCost() {
        harness.addToBattlefield(player1, new Dovescape());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Demonfire");
    }
}
