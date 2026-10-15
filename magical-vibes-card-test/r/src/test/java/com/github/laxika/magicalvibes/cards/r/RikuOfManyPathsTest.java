package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GeyserDrake;
import com.github.laxika.magicalvibes.cards.t.TrashTheTown;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RikuOfManyPaths.class, TrashTheTown.class, GeyserDrake.class})
class RikuOfManyPathsTest extends BaseCardTest {

    private static final String EXILE_MODE =
            "Exile the top card of your library. Until the end of your next turn, you may play it";
    private static final String COUNTER_MODE =
            "Put a +1/+1 counter on Riku of Many Paths. It gains trample until end of turn";
    private static final String TOKEN_MODE = "Create a 1/1 blue Bird creature token with flying";

    @Test
    @DisplayName("Exiles the top card when its mode is chosen")
    void exilesTopCard() {
        addRiku();
        harness.setHand(player1, List.of(new TrashTheTown()));
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake));

        castModal(EXILE_MODE);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(drake);
    }

    @Test
    @DisplayName("Adds a counter and grants trample until end of turn")
    void counterMode() {
        Permanent riku = addRiku();
        harness.setHand(player1, List.of(new TrashTheTown()));

        castModal(COUNTER_MODE);

        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, riku, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creates a blue Bird token with flying")
    void tokenMode() {
        addRiku();
        harness.setHand(player1, List.of(new TrashTheTown()));

        castModal(TOKEN_MODE);

        Permanent bird = findPermanents(player1, "Bird").getFirst();
        assertThat(bird.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addRiku() {
        return addCreatureReady(player1, new RikuOfManyPaths());
    }

    @Test
    void mayChooseNoModes() {
        Permanent riku = addRiku();
        harness.setHand(player1, List.of(new TrashTheTown()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castModalInstant(player1, 0, 2, List.of(riku.getId()));

        assertThatCode(() -> harness.handleListChoice(player1, "Choose no modes"))
                .doesNotThrowAnyException();
        resolveAllTriggers();

        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Bird")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void mayChooseOnlyOneModeWhenTwoWereChosenForSpell() {
        Permanent riku = addRiku();
        harness.setHand(player1, List.of(new TrashTheTown()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(riku.getId(), riku.getId()));
        harness.handleListChoice(player1, TOKEN_MODE);

        assertThatCode(() -> harness.handleListChoice(player1, "Done"))
                .doesNotThrowAnyException();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayChooseTwoDifferentModesForTwoModeSpell() {
        Permanent riku = addRiku();
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake));
        harness.setHand(player1, List.of(new TrashTheTown()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(riku.getId(), riku.getId()));
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, TOKEN_MODE);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(drake);
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }

    @Test
    void mayChooseSameModeForAnotherSpellInSameTurn() {
        addRiku();
        harness.setHand(player1, List.of(new TrashTheTown(), new TrashTheTown()));
        castModal(TOKEN_MODE);

        assertThatCode(() -> castModal(TOKEN_MODE)).doesNotThrowAnyException();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
    }

    @Test
    void laterSpellStillAllowsTwoModesInSameTurn() {
        Permanent riku = addRiku();
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake));
        harness.setHand(player1, List.of(new TrashTheTown(), new TrashTheTown()));
        castModal(TOKEN_MODE);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(riku.getId(), riku.getId()));
        harness.handleListChoice(player1, EXILE_MODE);

        assertThatCode(() -> harness.handleListChoice(player1, COUNTER_MODE))
                .doesNotThrowAnyException();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(drake);
        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonmodalSpellDoesNotTriggerRiku() {
        Permanent riku = addRiku();
        harness.setHand(player1, List.of(new GeyserDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Geyser Drake")).hasSize(1);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exiledCardCanBeCastByPayingItsManaCost() {
        addRiku();
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake));
        harness.setHand(player1, List.of(new TrashTheTown()));
        castModal(EXILE_MODE);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, drake.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(drake);
        assertThat(findPermanents(player1, "Geyser Drake")).hasSize(1);
    }

    @Test
    void trampleExpiresButCounterRemainsAfterTurn() {
        Permanent riku = addRiku();
        harness.setHand(player1, List.of(new TrashTheTown()));
        castModal(COUNTER_MODE);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, riku, Keyword.TRAMPLE)).isFalse();
        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void threeModeSpellAllowsAllThreeRikuModes() {
        Permanent riku = addRiku();
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake));
        harness.setHand(player1, List.of(new TrashTheTown()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castModalInstantWithModes(player1, 0, 1, 3,
                new int[]{0, 1, 2}, List.of(riku.getId(), riku.getId(), riku.getId()));
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(drake);
        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, riku, Keyword.TRAMPLE)).isTrue();
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        resolveAllTriggers();
        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void mayPlayExiledCardDuringNextTurn() {
        addRiku();
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake, new TrashTheTown(), new TrashTheTown()));
        harness.setHand(player1, List.of(new TrashTheTown()));
        castModal(EXILE_MODE);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castFromExile(player1, drake.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Geyser Drake")).hasSize(1);
    }

    @Test
    void cannotPlayExiledCardAfterNextTurnEnds() {
        addRiku();
        GeyserDrake drake = new GeyserDrake();
        harness.setLibrary(player1, List.of(drake, new TrashTheTown(), new TrashTheTown()));
        harness.setHand(player1, List.of(new TrashTheTown()));
        castModal(EXILE_MODE);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, drake.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(drake);
    }

    private void castModal(String rikuMode) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castModalInstant(player1, 0, 2,
                List.of(findPermanent(player1, "Riku of Many Paths").getId()));
        harness.handleListChoice(player1, rikuMode);
        resolveAllTriggers();
    }
}
