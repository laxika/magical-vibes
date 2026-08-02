package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorgerWurm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPaymentIntent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Add one mana of any colour" prompts raised while the player is paying for a specific spell grey
 * out the colours that would strand that payment.
 *
 * <p>Gorger Wurm costs {3}{R}{G}. Behind three Plains and a Forest, a Birds of Paradise is the only
 * red source on the battlefield, so red is the only colour it can produce that leaves the Wurm
 * castable — the Forest already covers {G}, and any other colour just wastes the bird.
 */
class ManaChoiceNarrowingTest extends BaseCardTest {

    /** Puts the bird onto the battlefield ready to tap, behind {@code plainsCount} Plains and a Forest. */
    private Permanent setUpBoard(int plainsCount, boolean withForest) {
        for (int i = 0; i < plainsCount; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        if (withForest) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player1, new BirdsOfParadise());
        GameData gameData = harness.getGameData();
        List<Permanent> battlefield = gameData.playerBattlefields.get(player1.getId());
        Permanent birds = battlefield.getLast();
        birds.setSummoningSick(false);
        return birds;
    }

    private PendingInteraction.ColorChoice activeColorChoice() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class);
    }

    private int birdsIndex() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
    }

    @Test
    @DisplayName("Only the colour no other source can produce stays enabled")
    void narrowsToTheOnlySourceOfAMissingColor() {
        harness.setHand(player1, List.of(new GorgerWurm()));
        setUpBoard(3, true);

        harness.activateAbilityToPayFor(player1, birdsIndex(), ManaPaymentIntent.forCast(0, 0));

        PendingInteraction.ColorChoice choice = activeColorChoice();
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(choice.disabledOptions()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "GREEN");
    }

    @Test
    @DisplayName("A greyed-out colour is still legally answerable")
    void disabledColorRemainsAnswerable() {
        harness.setHand(player1, List.of(new GorgerWurm()));
        setUpBoard(3, true);
        harness.activateAbilityToPayFor(player1, birdsIndex(), ManaPaymentIntent.forCast(0, 0));
        assertThat(activeColorChoice().disabledOptions()).contains("WHITE");

        harness.handleListChoice(player1, "WHITE");

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.playerManaPools.get(player1.getId())
                .get(com.github.laxika.magicalvibes.model.ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both missing colours stay enabled when either one can still be covered")
    void keepsEveryColorThatKeepsTheCastPayable() {
        // No Forest: the bird is the only source of {R} and of {G}, but three Plains cover the {3}.
        // Neither colour alone finishes the cost, so neither is a dead end and nothing is greyed out.
        harness.setHand(player1, List.of(new GorgerWurm()));
        setUpBoard(3, false);

        harness.activateAbilityToPayFor(player1, birdsIndex(), ManaPaymentIntent.forCast(0, 0));

        assertThat(activeColorChoice().disabledOptions()).isEmpty();
    }

    @Test
    @DisplayName("Without a declared payment every colour stays enabled")
    void leavesThePromptAloneWithoutAnIntent() {
        harness.setHand(player1, List.of(new GorgerWurm()));
        setUpBoard(3, true);

        harness.activateAbility(player1, birdsIndex(), null, null);

        assertThat(activeColorChoice().disabledOptions()).isEmpty();
    }

    @Test
    @DisplayName("A payment no colour can rescue leaves the prompt alone")
    void leavesThePromptAloneWhenNoColorHelps() {
        // One Plains and the bird cannot reach {3}{R}{G} however the bird is used; greying every
        // option would just look broken, so the prompt is left untouched.
        harness.setHand(player1, List.of(new GorgerWurm()));
        setUpBoard(1, false);

        harness.activateAbilityToPayFor(player1, birdsIndex(), ManaPaymentIntent.forCast(0, 0));

        assertThat(activeColorChoice().disabledOptions()).isEmpty();
    }
}
