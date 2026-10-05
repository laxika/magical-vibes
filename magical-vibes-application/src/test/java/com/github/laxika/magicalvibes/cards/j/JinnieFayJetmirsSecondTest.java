package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BestialMenace;
import com.github.laxika.magicalvibes.cards.c.CitizensCrowbar;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.r.RiveteersInitiate;
import com.github.laxika.magicalvibes.cards.s.StimulusPackage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JinnieFayJetmirsSecond.class, RaiseTheAlarm.class, BestialMenace.class, CitizensCrowbar.class, StimulusPackage.class, JaxisTheTroublemaker.class, RiveteersInitiate.class})
class JinnieFayJetmirsSecondTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a token creation event with Cat tokens")
    void createsCats() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "Cat");

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(cat -> {
            assertThat(cat.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(cat.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(cat.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
            assertThat(cat.getCard().getKeywords()).contains(Keyword.HASTE);
            assertThat(cat.getEffectivePower()).isEqualTo(2);
            assertThat(cat.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Replaces a token creation event with Dog tokens")
    void createsDogs() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "Dog");

        List<Permanent> dogs = findPermanents(player1, "Dog");
        assertThat(dogs).hasSize(2);
        assertThat(dogs).allSatisfy(dog -> {
            assertThat(dog.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(dog.getCard().getSubtypes()).containsExactly(CardSubtype.DOG);
            assertThat(dog.getCard().getKeywords()).contains(Keyword.VIGILANCE);
            assertThat(dog.getEffectivePower()).isEqualTo(3);
            assertThat(dog.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("May keep the original tokens")
    void keepsOriginalTokensWhenReplacementIsDeclined() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "Original tokens");

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("Does not replace an opponent's tokens")
    void opponentKeepsOriginalTokens() {
        harness.addToBattlefield(player2, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Cat")).isEmpty();
        assertThat(findPermanents(player2, "Cat")).isEmpty();
    }

    @Test
    @DisplayName("Replaces noncreature tokens and removes their original abilities")
    void replacesTreasureTokens() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new StimulusPackage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dog");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "Dog")).hasSize(2).allSatisfy(dog -> {
            assertThat(dog.getCard().getActivatedAbilities()).isEmpty();
            assertThat(dog.getCard().hasType(CardType.ARTIFACT)).isFalse();
        });
    }

    @Test
    @DisplayName("Equipment attaches to the replacement token")
    void crowbarAttachesToReplacementToken() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new CitizensCrowbar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cat");

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(findPermanent(player1, "Citizen's Crowbar").getAttachedTo()).isEqualTo(cat.getId());
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
    }

    @Test
    @DisplayName("Replaces all tokens in a simultaneous event with the chosen kind")
    void replacesEverySimultaneousToken() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new BestialMenace()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, "Cat");

        assertThat(findPermanents(player1, "Cat")).hasSize(3);
        assertThat(findPermanents(player1, "Snake")).isEmpty();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(findPermanents(player1, "Elephant")).isEmpty();
    }

    @Test
    @DisplayName("Declining preserves every different token in a simultaneous event")
    void declineKeepsEverySimultaneousToken() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new BestialMenace()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, "Original tokens");

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
    }

    @Test
    @DisplayName("May replace tokens created as copies")
    void replacesTokenCopy() {
        addCreatureReady(player1, new JaxisTheTroublemaker());
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.addToBattlefield(player1, new RiveteersInitiate());
        harness.setHand(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Riveteers Initiate"));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dog");

        assertThat(findPermanents(player1, "Dog")).hasSize(1);
        assertThat(findPermanents(player1, "Riveteers Initiate")).hasSize(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Dog"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Dog"), Keyword.VIGILANCE)).isTrue();
    }
}
