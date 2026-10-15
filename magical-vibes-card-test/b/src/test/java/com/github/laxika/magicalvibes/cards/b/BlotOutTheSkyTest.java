package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.cards.l.LoreholdCampus;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.cards.r.ReflectiveGolem;
import com.github.laxika.magicalvibes.cards.s.SparringRegimen;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlotOutTheSky.class, GrizzlyBears.class, ManaPrism.class, Plains.class,
        LetterOfAcceptance.class, LoreholdCampus.class, ReflectiveGolem.class, SparringRegimen.class})
class BlotOutTheSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X tapped 2/1 white and black flying Inkling tokens")
    void createsTappedInklingTokens() {
        harness.setHand(player1, List.of(new BlotOutTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 2);

        List<Permanent> inklings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Inkling".equals(permanent.getCard().getName()))
                .toList();
        assertThat(inklings).hasSize(2);
        assertThat(inklings).allSatisfy(inkling -> {
            assertThat(inkling.isTapped()).isTrue();
            assertThat(inkling.getCard().getPower()).isEqualTo(2);
            assertThat(inkling.getCard().getToughness()).isEqualTo(1);
            assertThat(inkling.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
            assertThat(inkling.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("At X=6, destroys noncreature nonland permanents and preserves creatures and lands")
    void thresholdDestroysNoncreatureNonlandPermanents() {
        harness.addToBattlefield(player1, new ManaPrism());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ManaPrism());
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new BlotOutTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 6);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ManaPrism)
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .anyMatch(permanent -> permanent.getCard().getName().equals("Inkling"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ManaPrism)
                .anyMatch(permanent -> permanent.getCard() instanceof Plains);
    }

    @Test
    @DisplayName("Below X=6, does not destroy noncreature nonland permanents")
    void belowThresholdDoesNotDestroyPermanents() {
        harness.addToBattlefield(player1, new ManaPrism());

        harness.setHand(player1, List.of(new BlotOutTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertOnBattlefield(player1, "Mana Prism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Inkling"));
    }

    @Test
    @DisplayName("X=0 creates no tokens and leaves noncreature permanents intact")
    void zeroCreatesNoTokensAndDoesNotDestroy() {
        harness.addToBattlefield(player2, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new BlotOutTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Letter of Acceptance");
        harness.assertInGraveyard(player1, "Blot Out the Sky");
    }

    @Test
    @DisplayName("X=5 creates five tokens without destroying artifacts or enchantments")
    void fiveIsBelowDestructionThreshold() {
        harness.addToBattlefield(player1, new SparringRegimen());
        harness.addToBattlefield(player2, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new BlotOutTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 5);

        harness.assertOnBattlefield(player1, "Sparring Regimen");
        harness.assertOnBattlefield(player2, "Letter of Acceptance");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> "Inkling".equals(permanent.getCard().getName()))
                .hasSize(5);
    }

    @Test
    @DisplayName("Above X=6 destroys artifacts and enchantments but preserves artifact creatures and lands")
    void aboveThresholdPreservesArtifactCreaturesAndAllTokens() {
        harness.addToBattlefield(player1, new SparringRegimen());
        harness.addToBattlefield(player2, new LetterOfAcceptance());
        harness.addToBattlefield(player2, new ReflectiveGolem());
        harness.addToBattlefield(player2, new LoreholdCampus());
        harness.setHand(player1, List.of(new BlotOutTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 7);

        harness.assertNotOnBattlefield(player1, "Sparring Regimen");
        harness.assertInGraveyard(player1, "Sparring Regimen");
        harness.assertNotOnBattlefield(player2, "Letter of Acceptance");
        harness.assertInGraveyard(player2, "Letter of Acceptance");
        harness.assertOnBattlefield(player2, "Reflective Golem");
        harness.assertOnBattlefield(player2, "Lorehold Campus");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(7)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Inkling");
                    assertThat(permanent.isTapped()).isTrue();
                });
    }
}
