package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UrzasMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorizonOfProgress.class, Forest.class, UrzasMine.class})
class HorizonOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Pays life and adds a type produced by a land you control")
    void paysLifeAndAddsManaFromControlledLand() {
        harness.addToBattlefield(player1, new HorizonOfProgress());
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Includes colorless mana types produced by controlled lands")
    void includesColorlessManaTypes() {
        harness.addToBattlefield(player1, new HorizonOfProgress());
        harness.addToBattlefield(player1, new UrzasMine());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("May put a land from hand onto the battlefield tapped")
    void putsLandFromHandTapped() {
        harness.addToBattlefield(player1, new HorizonOfProgress());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(enteredForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Horizon of Progress draws a card")
    void sacrificesAndDraws() {
        harness.addToBattlefield(player1, new HorizonOfProgress());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof HorizonOfProgress);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof HorizonOfProgress);
    }
}
