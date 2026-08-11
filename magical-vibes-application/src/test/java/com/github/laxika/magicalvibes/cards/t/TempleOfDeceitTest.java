package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempleOfDeceitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and triggers scry 1")
    void entersTappedAndScries() {
        harness.setHand(player1, List.of(new TempleOfDeceit()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        Permanent temple = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                    assertThat(entry.getCard().getName()).isEqualTo("Temple of Deceit");
                });

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Can tap for blue or black mana")
    void tapsForBlueOrBlackMana() {
        harness.addToBattlefield(player1, new TempleOfDeceit());
        harness.addToBattlefield(player1, new TempleOfDeceit());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
