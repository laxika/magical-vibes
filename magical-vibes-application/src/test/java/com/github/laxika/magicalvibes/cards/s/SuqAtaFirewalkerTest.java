package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrangerGuildmage;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.r.RecklessEmbermage;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuqAtaFirewalker.class, Incinerate.class, Boomerang.class,
        RecklessEmbermage.class, GrangerGuildmage.class})
class SuqAtaFirewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's red spells cannot target Suq'Ata Firewalker")
    void opponentRedSpellsCannotTarget() {
        harness.addToBattlefield(player2, new SuqAtaFirewalker());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("The controller's own red spells cannot target Suq'Ata Firewalker either")
    void ownRedSpellsCannotTarget() {
        harness.addToBattlefield(player1, new SuqAtaFirewalker());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player1, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("Nonred spells can target Suq'Ata Firewalker")
    void nonRedSpellsCanTarget() {
        harness.addToBattlefield(player2, new SuqAtaFirewalker());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Suq'Ata Firewalker"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Boomerang"));
    }

    @Test
    @DisplayName("Abilities from red sources cannot target Suq'Ata Firewalker")
    void redSourceAbilitiesCannotTarget() {
        harness.addToBattlefield(player2, new SuqAtaFirewalker());
        addCreatureReady(player1, new RecklessEmbermage());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("The controller's own red source abilities cannot target Suq'Ata Firewalker")
    void ownRedSourceAbilitiesCannotTarget() {
        addCreatureReady(player1, new RecklessEmbermage());
        harness.addToBattlefield(player1, new SuqAtaFirewalker());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player1, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("Abilities from nonred sources can target Suq'Ata Firewalker")
    void nonRedSourceAbilitiesCanTarget() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new SuqAtaFirewalker());
        addCreatureReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Suq'Ata Firewalker"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suq'Ata Firewalker");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void tapAbilityDeals1Damage() {
        harness.setLife(player2, 20);
        Permanent firewalker = addCreatureReady(player1, new SuqAtaFirewalker());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(firewalker.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
