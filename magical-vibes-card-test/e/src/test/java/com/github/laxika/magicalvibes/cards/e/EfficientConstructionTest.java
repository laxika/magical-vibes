package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EfficientConstruction.class, Spellbook.class, GrizzlyBears.class, Ornithopter.class})
class EfficientConstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact creates a 1/1 colorless Thopter artifact creature token with flying")
    void artifactSpellCreatesThopter() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not create a Thopter")
    void nonartifactSpellDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting an artifact does not create a Thopter")
    void opponentArtifactSpellDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Spellbook(), "{0}");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An artifact creature cast creates a Thopter before the spell resolves")
    void artifactCreatureTriggersBeforeResolving() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.castFromHand(player1, new Ornithopter(), "{0}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Ornithopter);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Ornithopter);
    }

    @Test
    @DisplayName("Putting an artifact onto the battlefield without casting does not trigger")
    void artifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new EfficientConstruction());

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Each Efficient Construction triggers independently")
    void multipleCopiesEachCreateToken() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.castFromHand(player1, new Ornithopter(), "{0}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Every artifact cast triggers, including subsequent spells in the same turn")
    void subsequentArtifactSpellsEachTrigger() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }
}
