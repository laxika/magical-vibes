package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.b.Broodstar;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.cards.g.GrabTheReins;
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

@CardUsed({Duplicant.class, GoblinStriker.class, AetherSpellbomb.class, Broodstar.class, GrabTheReins.class})
class DuplicantTest extends BaseCardTest {

    private void castDuplicantAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Duplicant(), "{6}");
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
        harness.castFromHand(player1, new Duplicant(), "{6}");
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
        harness.castFromHand(player1, new Duplicant(), "{6}");
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
        harness.castFromHand(player1, new Duplicant(), "{6}");
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

    @Test
    @DisplayName("Exiled Broodstar defines Duplicant's stats using its owner's artifacts")
    void evaluatesCharacteristicDefiningAbilityInExile() {
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        var broodstar = harness.addToBattlefieldAndReturn(player2, new Broodstar());

        castDuplicantAndAcceptMay(broodstar.getId());

        harness.assertOnBattlefield(player1, "Duplicant");
        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactlyInAnyOrder(CardSubtype.BEAST, CardSubtype.SHAPESHIFTER);

        harness.addToBattlefield(player2, new AetherSpellbomb());

        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(3);
    }

    @Test
    @DisplayName("A target returned to hand before resolution is not exiled")
    void targetLeavesBeforeImprintResolves() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        var goblin = harness.addToBattlefieldAndReturn(player2, new GoblinStriker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Duplicant(), "{6}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Goblin Striker");
        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Duplicant and recasting it does not retain its old imprint")
    void recastDuplicantDoesNotRetainImprint() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        var goblin = harness.addToBattlefieldAndReturn(player2, new GoblinStriker());
        castDuplicantAndAcceptMay(goblin.getId());
        Permanent oldDuplicant = findPermanent(player1, "Duplicant");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, oldDuplicant.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Duplicant");

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent duplicant = findPermanent(player1, "Duplicant");
        harness.handlePermanentChosen(player1, duplicant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactly(CardSubtype.SHAPESHIFTER);
        harness.assertNotOnBattlefield(player2, "Goblin Striker");
    }
    @Test
    @DisplayName("Changing Duplicant's controller does not break its pending imprint")
    void controlChangeBeforeResolutionPreservesImprint() {
        var goblin = harness.addToBattlefieldAndReturn(player2, new GoblinStriker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Duplicant(), "{6}");
        harness.passBothPriorities();
        Permanent duplicant = findPermanent(player1, "Duplicant");
        harness.handlePermanentChosen(player1, goblin.getId());

        harness.setHand(player2, List.of(new GrabTheReins()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(duplicant.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Duplicant");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goblin Striker");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.BERSERKER, CardSubtype.SHAPESHIFTER);
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
