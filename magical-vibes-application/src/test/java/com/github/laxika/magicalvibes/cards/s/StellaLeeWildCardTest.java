package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({StellaLeeWildCard.class, DarkRitual.class, GrizzlyBears.class, LightningBolt.class})
class StellaLeeWildCardTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card on your second spell and lets you play it until your next turn ends")
    void exilesTopCardOnSecondSpell() {
        Card top = new LightningBolt();
        harness.setLibrary(player1, List.of(top));
        addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
    }

    @Test
    @DisplayName("Copies a controlled instant or sorcery after three spells have been cast")
    void copiesControlledSpellAfterThreeSpells() {
        DarkRitual target = new DarkRitual();
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual(), target));
        harness.addMana(player1, ManaColor.BLACK, 1);

        for (int i = 0; i < 3; i++) {
            harness.castInstant(player1, 0);
            harness.passBothPriorities();
        }

        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        harness.castInstant(player1, 0);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot activate before three spells have been cast")
    void cannotActivateBeforeThreeSpells() {
        DarkRitual target = new DarkRitual();
        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more spells");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        for (int i = 0; i < 3; i++) {
            harness.castInstant(player1, 0);
            harness.passBothPriorities();
        }

        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
