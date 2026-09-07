package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoilingRockRioter.class, BojukaBrigand.class, GrizzlyBears.class})
class BoilingRockRioterTest extends BaseCardTest {

    @Test
    void attackingAddsRedManaUntilEndOfCombat() {
        addRioterReady();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void tapsAnAllyToExileTargetCardFromAGraveyard() {
        Permanent rioter = addRioterReady();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(rioter.isTapped()).isTrue();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(rioter.getId())).containsExactly(target);
    }

    @Test
    void attackingPermitsCastingOneOwnedAllyForItsNormalCost() {
        Permanent rioter = addRioterReady();
        Card ownAlly = new BojukaBrigand();
        Card ownNonAlly = new GrizzlyBears();
        Card opponentAlly = new BojukaBrigand();
        gd.addToExile(player1.getId(), ownAlly, rioter.getId());
        gd.addToExile(player1.getId(), ownNonAlly, rioter.getId());
        gd.addToExile(player2.getId(), opponentAlly, rioter.getId());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .contains(ownAlly.getId())
                .doesNotContain(ownNonAlly.getId(), opponentAlly.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, ownAlly.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bojuka Brigand");
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .doesNotContain(ownAlly.getId());
    }

    private Permanent addRioterReady() {
        return addCreatureReady(player1, new BoilingRockRioter());
    }
}
