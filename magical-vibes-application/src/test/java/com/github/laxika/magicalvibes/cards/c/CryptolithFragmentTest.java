package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptolithFragment.class})
class CryptolithFragmentTest extends BaseCardTest {

    private static final int STARTING_LIFE = GameData.STARTING_LIFE_TOTAL;

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CryptolithFragment()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cryptolith Fragment").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds the chosen color and makes each player lose 1 life")
    void tapAddsManaAndDrainsEveryone() {
        Permanent fragment = addReadyFragment(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(STARTING_LIFE - 1);
        assertThat(fragment.isTapped()).isTrue();
        // Mana ability — never uses the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not transform at upkeep while an opponent is above 10 life")
    void doesNotTransformWhenOpponentAbove10() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isFalse();
        assertThat(fragment.getCard().getName()).isEqualTo("Cryptolith Fragment");
    }

    @Test
    @DisplayName("Transforms at upkeep when every player is at 10 or less life")
    void transformsWhenEveryoneAtOrBelow10() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isTrue();
        assertThat(fragment.getCard().getName()).isEqualTo("Aurora of Emrakul");
    }

    @Test
    @DisplayName("Back face attack trigger makes each opponent lose 3 life")
    void backFaceAttackDrainsOpponents() {
        Permanent aurora = addTransformedFragment(player1);
        harness.setLife(player1, STARTING_LIFE);
        harness.setLife(player2, STARTING_LIFE);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aurora)));
        resolveAllTriggers();

        // 3 from the attack trigger plus 1 unblocked combat damage
        assertThat(gd.getLife(player2.getId())).isEqualTo(STARTING_LIFE - 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void summoningSicknessDoesNotRestrictArtifactManaAbility(ManaColor color) {
        Permanent fragment = harness.addToBattlefieldAndReturn(player1, new CryptolithFragment());
        fragment.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(STARTING_LIFE - 1);
        assertThat(fragment.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTransformWhenControllerAbove10() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 11);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformDuringOpponentsUpkeep() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isFalse();
    }

    @Test
    void rechecksLifeTotalsWhenUpkeepTriggerResolves() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player2, 11);
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isFalse();
    }

    @Test
    void droppingTo10AfterUpkeepBeginsDoesNotCreateTrigger() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 11);
        harness.setLife(player2, 11);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(fragment.isTransformed()).isFalse();
    }

    @Test
    void transformingPreservesTappedStateAndDoesNotTriggerOnLaterUpkeeps() {
        Permanent fragment = addReadyFragment(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isTrue();
        assertThat(fragment.isTapped()).isTrue();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fragment.isTransformed()).isTrue();
        assertThat(fragment.isTapped()).isFalse();
    }

    @Test
    void backFaceAttackTriggerDrainsOnlyOpponentsBeforeCombatDamage() {
        Permanent aurora = addTransformedFragment(player2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE - 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(STARTING_LIFE);
        assertThat(aurora.isTapped()).isTrue();
    }

    private Permanent addReadyFragment(Player player) {
        return addCreatureReady(player, new CryptolithFragment());
    }

    private Permanent addTransformedFragment(Player player) {
        Permanent perm = addReadyFragment(player);
        perm.setCard(perm.getCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }
}
