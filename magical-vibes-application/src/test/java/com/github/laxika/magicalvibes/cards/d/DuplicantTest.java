package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Duplicant.class, GoblinStriker.class, AetherSpellbomb.class})
class DuplicantTest extends BaseCardTest {

    private void castDuplicantAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Exiles a nontoken creature and takes its power, toughness, and creature types")
    void copiesExiledCreatureCharacteristics() {
        harness.addToBattlefield(player2, new GoblinStriker());
        UUID goblinId = harness.getPermanentId(player2, "Goblin Striker");

        castDuplicantAndAcceptMay(goblinId);

        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.BERSERKER, CardSubtype.SHAPESHIFTER);
        harness.assertNotOnBattlefield(player2, "Goblin Striker");
    }

    @Test
    @DisplayName("Does not copy keywords from the exiled creature")
    void doesNotCopyKeywordsFromExiledCreature() {
        harness.addToBattlefield(player2, new GoblinStriker());
        UUID goblinId = harness.getPermanentId(player2, "Goblin Striker");

        castDuplicantAndAcceptMay(goblinId);

        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.hasKeyword(gd, duplicant, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, duplicant, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining the imprint keeps Duplicant's printed characteristics")
    void decliningMayKeepsPrintedCharacteristics() {
        harness.addToBattlefield(player2, new GoblinStriker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Goblin Striker"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactly(CardSubtype.SHAPESHIFTER);
        harness.assertOnBattlefield(player2, "Goblin Striker");
    }

    @Test
    @DisplayName("Duplicant itself is a legal target when no other nontoken creature is available")
    void canTargetItselfWhenNoOtherNontokenCreatureExists() {
        harness.addToBattlefield(player2, new AetherSpellbomb());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice.class,
                        choice -> assertThat(choice.validPermanentIds()).containsExactly(duplicant.getId()));
        harness.assertOnBattlefield(player2, "Aether Spellbomb");
    }

    @Test
    @DisplayName("Target selection excludes token creatures")
    void tokenIsNotALegalTarget() {
        harness.addToBattlefield(player2, tokenCreature());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicant = findPermanent(player1, "Duplicant");
        UUID tokenId = harness.getPermanentId(player2, "Bear Token");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice.class,
                        choice -> {
                            assertThat(choice.validPermanentIds()).containsExactly(duplicant.getId());
                            assertThat(choice.validPermanentIds()).doesNotContain(tokenId);
                        });
        harness.assertOnBattlefield(player2, "Bear Token");
    }

    private com.github.laxika.magicalvibes.model.Card tokenCreature() {
        com.github.laxika.magicalvibes.model.Card token = new com.github.laxika.magicalvibes.model.Card();
        token.setName("Bear Token");
        token.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        token.setPower(2);
        token.setToughness(2);
        token.setSubtypes(List.of(CardSubtype.BEAR));
        token.setToken(true);
        return token;
    }
}
