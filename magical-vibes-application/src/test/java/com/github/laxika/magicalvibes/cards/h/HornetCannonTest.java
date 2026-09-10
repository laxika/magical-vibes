package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TorporOrb;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HornetCannon.class)
class HornetCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Pays three generic mana and taps Hornet Cannon to activate")
    void paysActivationCostAndTapsCannon() {
        Permanent cannon = addHornetCannon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(cannon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a 1/1 colorless flying, hasty Insect artifact creature token")
    void createsHornetToken() {
        Permanent token = createHornetToken(player1);

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isNull();
    }

    @Test
    @CardUsed(TorporOrb.class)
    @DisplayName("Destroys the Hornet token even when Torpor Orb suppresses creature ETB triggers")
    void delayedDestructionIsNotSuppressedByTorporOrb() {
        harness.addToBattlefield(player1, new TorporOrb());
        createHornetToken(player1);
        harness.assertOnBattlefield(player1, "Hornet");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hornet");
    }

    @Test
    @DisplayName("Destroys the Hornet token at the beginning of the next end step")
    void destroysHornetTokenAtNextEndStep() {
        createHornetToken(player1);
        harness.assertOnBattlefield(player1, "Hornet");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hornet");
    }

    private Permanent createHornetToken(Player player) {
        addHornetCannon(player);
        harness.addMana(player, ManaColor.COLORLESS, 3);

        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Hornet");
    }

    private Permanent addHornetCannon(Player player) {
        Permanent cannon = harness.addToBattlefieldAndReturn(player, new HornetCannon());
        cannon.setSummoningSick(false);
        return cannon;
    }
}
