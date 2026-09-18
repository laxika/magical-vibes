package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({KirtarsWrath.class, DuskImp.class, Forest.class})
class KirtarsWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Without threshold, Kirtar's Wrath destroys all creatures and creates no tokens")
    void withoutThresholdDestroysCreaturesOnly() {
        harness.addToBattlefield(player1, new DuskImp());
        harness.addToBattlefield(player2, new DuskImp());
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()));
        harness.setGraveyard(player2, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dusk Imp");
        harness.assertNotOnBattlefield(player2, "Dusk Imp");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertInGraveyard(player2, "Dusk Imp");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("With threshold, Kirtar's Wrath destroys all creatures and creates two flying Spirits")
    void withThresholdCreatesSpirits() {
        harness.addToBattlefield(player1, new DuskImp());
        harness.addToBattlefield(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertInGraveyard(player2, "Dusk Imp");
        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCard().isToken()).isTrue();
            assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(spirit.getCard().getPower()).isEqualTo(1);
            assertThat(spirit.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Kirtar's Wrath cannot be stopped by a regeneration shield")
    void destructionCannotBeRegenerated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        creature.setRegenerationShield(1);
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("Indestructible creatures survive Kirtar's Wrath")
    void indestructibleCreaturesSurvive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dusk Imp");
        harness.assertNotInGraveyard(player2, "Dusk Imp");
    }
}
