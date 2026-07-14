package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FableOfWolfAndOwlTest extends BaseCardTest {

    private long tokenCount(UUID playerId, String name) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .count();
    }

    @Test
    @DisplayName("Casting a green spell may create a 2/2 green Wolf token")
    void greenSpellCreatesWolf() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the token trigger

        assertThat(tokenCount(player1.getId(), "Wolf")).isEqualTo(1);
        assertThat(tokenCount(player1.getId(), "Bird")).isZero();
    }

    @Test
    @DisplayName("Casting a blue spell may create a 1/1 blue flying Bird token")
    void blueSpellCreatesBird() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the token trigger

        assertThat(tokenCount(player1.getId(), "Bird")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bird"))
                .anyMatch(p -> p.getCard().getKeywords().contains(Keyword.FLYING))).isTrue();
    }

    @Test
    @DisplayName("Declining the green trigger creates no token")
    void declineCreatesNoToken() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tokenCount(player1.getId(), "Wolf")).isZero();
    }

    @Test
    @DisplayName("Casting a red spell triggers neither ability")
    void redSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(tokenCount(player1.getId(), "Wolf")).isZero();
        assertThat(tokenCount(player1.getId(), "Bird")).isZero();
    }
}
