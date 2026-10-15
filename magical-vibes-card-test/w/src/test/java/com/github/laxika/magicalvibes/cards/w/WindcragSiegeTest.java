package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HungrySpriggan;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindcragSiege.class, HungrySpriggan.class, EsixFractalBloom.class})
class WindcragSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Jeskai creates a hasty lifelinking Goblin at upkeep")
    void jeskaiCreatesGoblinAtUpkeep() {
        castAndChoose("Jeskai");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> goblins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(goblins).hasSize(1);
        Permanent goblin = goblins.getFirst();
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(goblin.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Mardu makes direct attack triggers trigger twice")
    void marduDoublesDirectAttackTrigger() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());
        castAndChoose("Mardu");

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(6);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("Jeskai does not create tokens at an opponent's upkeep")
    void jeskaiDoesNotTriggerAtOpponentUpkeep() {
        castAndChoose("Jeskai");

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mardu does not create a Goblin at upkeep")
    void marduDoesNotCreateGoblin() {
        castAndChoose("Mardu");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Jeskai does not double attack triggers")
    void jeskaiDoesNotDoubleAttackTriggers() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());
        castAndChoose("Jeskai");

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(3);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Mardu does not double an opponent's attack triggers")
    void marduDoesNotDoubleOpponentTriggers() {
        Permanent spriggan = addCreatureReady(player2, new HungrySpriggan());
        castAndChoose("Mardu");

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(3);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Mardu Sieges make an attack ability trigger three times")
    void multipleMarduSiegesAddTriggers() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());
        castAndChoose("Mardu");
        castAndChoose("Mardu");

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(9);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(9);
    }

    @Test
    @DisplayName("A Jeskai Goblin remains after its lifelink and haste expire")
    void goblinKeywordsExpireAtEndOfTurn() {
        castAndChoose("Jeskai");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(goblin.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goblin);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(goblin.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Jeskai grants lifelink and haste to the token substituted by Esix")
    void esixReplacementTokenStillGainsKeywords() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());
        addCreatureReady(player2, new HungrySpriggan());
        castAndChoose("Jeskai");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, spriggan.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
                    assertThat(token.hasKeyword(Keyword.LIFELINK)).isTrue();
                    assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    private void castAndChoose(String mode) {
        harness.castFromHand(player1, new WindcragSiege(), "{1}{R}{W}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Mardu", "Jeskai");
        harness.handleListChoice(player1, mode);
    }
}
