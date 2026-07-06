package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThat;

class StressDreamTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player p) {
        harness.addMana(p, ManaColor.BLUE, 1);
        harness.addMana(p, ManaColor.RED, 1);
        harness.addMana(p, ManaColor.GREEN, 3); // 3 generic
    }

    // ===== Damage + card selection =====

    @Test
    @DisplayName("Deals 5 to target creature; chosen card to hand, other to bottom")
    void damagesCreatureAndSelectsCard() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(0, top2);
        gd.playerDecks.get(player1.getId()).add(0, top1); // top1 is now the very top

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        // Choose top1 to hand; top2 goes to the bottom
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        GameData gd = harness.getGameData();
        // Avatar (8/8) survives 5 damage
        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        // Chosen card in hand
        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        // Other card on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top2);
    }

    // ===== No creature target (up to one) =====

    @Test
    @DisplayName("Can be cast with no creature target; still looks at two and takes one")
    void castWithNoCreatureTarget() {
        harness.setHand(player1, List.of(new StressDream()));
        addMana(player1);

        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(0, top2);
        gd.playerDecks.get(player1.getId()).add(0, top1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId())); // keep top2 instead

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top1);
        harness.assertInGraveyard(player1, "Stress Dream");
    }
}
