package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptCreeper.class, DuskImp.class, Forest.class})
class CryptCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing exiles a creature card from an opponent's graveyard")
    void exilesCreatureFromOpponentGraveyard() {
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        Card target = new DuskImp();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Dusk Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dusk Imp"));
    }

    @Test
    @DisplayName("Can exile a noncreature card from the controller's own graveyard")
    void exilesNoncreatureFromOwnGraveyard() {
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        Card target = new Forest();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Activating sacrifices Crypt Creeper")
    void activatingSacrificesCreeper() {
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        Card target = new DuskImp();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creeper);
        harness.assertInGraveyard(player1, "Crypt Creeper");
    }

    @Test
    @DisplayName("Requires a graveyard card target")
    void requiresGraveyardCardTarget() {
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ability requires a target");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creeper);
        harness.assertNotInGraveyard(player1, "Crypt Creeper");
    }

    @Test
    @DisplayName("Rejects a target that is not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        Card target = new DuskImp();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        Card target = new DuskImp();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Dusk Imp"));
    }

    @Test
    @DisplayName("Can activate while summoning sick (no tap cost)")
    void canActivateWithSummoningSickness() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new CryptCreeper());
        creeper.setSummoningSick(true);

        Card target = new DuskImp();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dusk Imp"));
    }
}
