package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfFoolsWisdom.class, Forest.class})
class CurseOfFoolsWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Curse of Fool's Wisdom attaches it to the target player")
    void resolvingAttachesToTargetPlayer() {
        harness.setHand(player1, List.of(new CurseOfFoolsWisdom()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CurseOfFoolsWisdom
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Enchanted player's draw makes them lose 2 life and the controller gain 2 life")
    void drainsWhenEnchantedPlayerDraws() {
        attachCurse(player1, player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new Forest()));

        draw(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A draw by another player does not trigger Curse of Fool's Wisdom")
    void doesNotTriggerForAnotherPlayerDraw() {
        attachCurse(player1, player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Curse of Fool's Wisdom can enchant its controller")
    void canEnchantController() {
        harness.setHand(player1, List.of(new CurseOfFoolsWisdom()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CurseOfFoolsWisdom
                        && permanent.getAttachedTo().equals(player1.getId()));
    }

    private Permanent attachCurse(Player controller, Player enchantedPlayer) {
        Permanent curse = new Permanent(new CurseOfFoolsWisdom());
        curse.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(curse);
        return curse;
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
