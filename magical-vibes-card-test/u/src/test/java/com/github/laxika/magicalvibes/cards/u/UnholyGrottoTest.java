package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EntrailsFeaster;
import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnholyGrotto.class, EntrailsFeaster.class, FutureSight.class})
class UnholyGrottoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent grotto = addReadyGrotto();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(grotto.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target Zombie card from the graveyard on top of the library")
    void putsTargetZombieOnTopOfLibrary() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card zombie = new EntrailsFeaster();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setLibrary(player1, List.of(new FutureSight()));

        int grottoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grotto);
        harness.activateAbilityWithGraveyardTargets(player1, grottoIndex, 1, List.of(zombie.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(zombie);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(zombie);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(grotto.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Zombie-return ability requires black mana")
    void requiresBlackMana() {
        Permanent grotto = addReadyGrotto();
        Card zombie = new EntrailsFeaster();
        harness.setGraveyard(player1, List.of(zombie));

        int grottoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grotto);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, grottoIndex, 1, List.of(zombie.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
        assertThat(grotto.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only Zombie cards in your graveyard are legal targets")
    void rejectsNonZombieTarget() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card nonZombie = new FutureSight();
        harness.setGraveyard(player1, List.of(nonZombie));

        int grottoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grotto);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, grottoIndex, 1, List.of(nonZombie.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only cards in your graveyard can be targeted")
    void rejectsZombieInOpponentsGraveyard() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card opponentZombie = new EntrailsFeaster();
        harness.setGraveyard(player2, List.of(opponentZombie));

        int grottoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grotto);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, grottoIndex, 1, List.of(opponentZombie.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Puts the chosen Zombie on top of an empty library and leaves other cards alone")
    void returnsOnlyChosenZombieToEmptyLibrary() {
        addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card chosen = new EntrailsFeaster();
        Card other = new EntrailsFeaster();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.setLibrary(player1, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("A Zombie that leaves the graveyard before resolution is not returned")
    void doesNotReturnMissingTarget() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card zombie = new EntrailsFeaster();
        Card libraryCard = new FutureSight();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(zombie.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(grotto.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Grotto cannot activate its Zombie-return ability")
    void cannotActivateWhileTapped() {
        Permanent grotto = addReadyGrotto();
        grotto.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card zombie = new EntrailsFeaster();
        harness.setGraveyard(player1, List.of(zombie));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(zombie.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private Permanent addReadyGrotto() {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new UnholyGrotto());
        grotto.setSummoningSick(false);
        return grotto;
    }
}
