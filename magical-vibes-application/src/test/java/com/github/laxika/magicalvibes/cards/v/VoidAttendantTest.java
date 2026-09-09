package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidAttendant.class, PathToExile.class})
class VoidAttendantTest extends BaseCardTest {

    @Test
    void processesOpponentOwnedExiledCardAndCreatesEldraziScion() {
        addReadyAttendant();
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Path to Exile");
        harness.passBothPriorities();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        assertThat(scion.getCard().isToken()).isTrue();
    }

    @Test
    void eldraziScionCanBeSacrificedForColorlessMana() {
        addReadyAttendant();
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyAttendant() {
        Permanent attendant = new Permanent(new VoidAttendant());
        attendant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attendant);
        return attendant;
    }
}
