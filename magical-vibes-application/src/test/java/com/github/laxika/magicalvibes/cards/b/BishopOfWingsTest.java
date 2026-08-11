package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.StarlitAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BishopOfWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life when an Angel you control enters")
    void gainsLifeWhenAllyAngelEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.setHand(player1, List.of(new StarlitAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Does not gain life when a non-Angel creature enters")
    void doesNotGainLifeWhenNonAngelEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Creates a 1/1 white flying Spirit when an Angel you control dies")
    void createsSpiritWhenAllyAngelDies() {
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.addToBattlefield(player1, new StarlitAngel());

        destroyWithMurder(player2, player1, "Starlit Angel");
        harness.passBothPriorities();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    private void destroyWithMurder(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.getGameService().playCard(harness.getGameData(), caster, 0, 0, targetId, null);
        harness.passBothPriorities();
    }
}
