package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyFragment(Player player) {
        CryptolithFragment card = new CryptolithFragment();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedFragment(Player player) {
        CryptolithFragment card = new CryptolithFragment();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
