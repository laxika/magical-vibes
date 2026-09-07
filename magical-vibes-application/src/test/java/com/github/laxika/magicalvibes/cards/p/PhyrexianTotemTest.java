package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianTotem.class, GrizzlyBears.class, Shock.class})
class PhyrexianTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Phyrexian Totem adds black mana")
    void tappingAddsBlackMana() {
        Permanent totem = addReadyTotem(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Phyrexian Totem becomes a black 5/5 Phyrexian Horror with trample")
    void animatesIntoPhyrexianHorror() {
        Permanent totem = addReadyTotem(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.isArtifact(totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, totem)).containsExactly(CardColor.BLACK);
        assertThat(totem.getTransientSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(gqs.hasKeyword(gd, totem, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isFalse();
    }

    @Test
    @DisplayName("Damage to the animated Totem makes its controller sacrifice that many permanents")
    void animatedTotemMakesControllerSacrificeThatManyPermanents() {
        Permanent totem = addReadyTotem(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        int totemIndex = gd.playerBattlefields.get(player2.getId()).indexOf(totem);
        harness.activateAbility(player2, totemIndex, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, totem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        List<Permanent> permanents = gameData.playerBattlefields.get(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, permanents.subList(1, 3).stream()
                .map(Permanent::getId)
                .toList());

        assertThat(gameData.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Phyrexian Totem");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyTotem(Player player) {
        Permanent totem = new Permanent(new PhyrexianTotem());
        totem.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(totem);
        return totem;
    }
}
