package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ApocalypseChime;
import com.github.laxika.magicalvibes.cards.w.WallOfKelp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrudgeSpell.class, DeathSpeakers.class, DrySpell.class, ApocalypseChime.class, WallOfKelp.class})
class DrudgeSpellTest extends BaseCardTest {

    private void createSkeleton() {
        createSkeleton(player1, 0);
    }

    private void createSkeleton(Player player, int permanentIndex) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.activateAbility(player, permanentIndex, null, null);
        harness.passBothPriorities();
    }

    private void removeDrudgeSpellWithApocalypseChime() {
        harness.addToBattlefield(player2, new ApocalypseChime());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {B} and exiling two creature cards creates a Skeleton token")
    void createsSkeletonToken() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new DeathSpeakers(), new DeathSpeakers()));

        createSkeleton();

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without two creature cards in the graveyard")
    void requiresTwoCreatureCards() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new DeathSpeakers(), new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Skeleton")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Skeleton token can regenerate itself for {B}")
    void skeletonRegenerates() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new DeathSpeakers(), new DeathSpeakers()));
        createSkeleton();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DrySpell()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
    }

    @Test
    @DisplayName("All Skeleton tokens are destroyed when Drudge Spell leaves the battlefield")
    void destroysSkeletonsOnLeave() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new DeathSpeakers(), new DeathSpeakers()));
        createSkeleton();
        addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        assertThat(countPermanents(player1, "Kelp")).isEqualTo(1);

        removeDrudgeSpellWithApocalypseChime();

        harness.assertNotOnBattlefield(player1, "Drudge Spell");
        assertThat(countPermanents(player1, "Skeleton")).isZero();
        assertThat(countPermanents(player1, "Kelp")).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield does not save the Skeletons from the leave trigger")
    void skeletonsCannotBeRegeneratedOnLeave() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new DeathSpeakers(), new DeathSpeakers()));
        createSkeleton();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        removeDrudgeSpellWithApocalypseChime();

        assertThat(countPermanents(player1, "Skeleton")).isZero();
    }

    @Test
    @DisplayName("The leave trigger destroys Skeleton tokens controlled by either player")
    void destroysSkeletonTokensControlledByEitherPlayer() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new DeathSpeakers(), new DeathSpeakers()));
        createSkeleton();

        harness.addToBattlefield(player2, new ApocalypseChime());
        harness.addToBattlefield(player2, new DrudgeSpell());
        harness.setGraveyard(player2, List.of(new DeathSpeakers(), new DeathSpeakers()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        createSkeleton(player2, 1);

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        assertThat(countPermanents(player2, "Skeleton")).isEqualTo(1);

        removeDrudgeSpellWithApocalypseChime();

        assertThat(countPermanents(player1, "Skeleton")).isZero();
        assertThat(countPermanents(player2, "Skeleton")).isZero();
    }
}
