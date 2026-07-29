package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SacredMesaTest extends BaseCardTest {

    private long pegasusTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Pegasus".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("Activated ability creates a 1/1 white Pegasus token with flying")
    void activatedAbilityCreatesPegasusToken() {
        harness.addToBattlefield(player1, new SacredMesa());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pegasusTokens()).isEqualTo(1);

        Permanent token = findPermanent(player1, "Pegasus");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Upkeep with no Pegasus sacrifices Sacred Mesa without prompting")
    void upkeepWithoutPegasusSacrificesMesa() {
        harness.addToBattlefield(player1, new SacredMesa());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.assertNotOnBattlefield(player1, "Sacred Mesa");
        harness.assertInGraveyard(player1, "Sacred Mesa");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a Pegasus at upkeep keeps Sacred Mesa")
    void sacrificingPegasusKeepsMesa() {
        harness.addToBattlefield(player1, new SacredMesa());
        harness.addToBattlefield(player1, new StormfrontPegasus());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID pegasusId = findPermanent(player1, "Stormfront Pegasus").getId();
        harness.handlePermanentChosen(player1, pegasusId);

        harness.assertOnBattlefield(player1, "Sacred Mesa");
        harness.assertNotOnBattlefield(player1, "Stormfront Pegasus");
        harness.assertInGraveyard(player1, "Stormfront Pegasus");
    }

    @Test
    @DisplayName("Declining to sacrifice a Pegasus sacrifices Sacred Mesa")
    void decliningSacrificesMesa() {
        harness.addToBattlefield(player1, new SacredMesa());
        harness.addToBattlefield(player1, new StormfrontPegasus());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Sacred Mesa");
        harness.assertInGraveyard(player1, "Sacred Mesa");
        harness.assertOnBattlefield(player1, "Stormfront Pegasus");
    }

    @Test
    @DisplayName("A Pegasus token created by Sacred Mesa can pay its own upkeep cost")
    void pegasusTokenPaysUpkeepCost() {
        harness.addToBattlefield(player1, new SacredMesa());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Pegasus").getId());

        harness.assertOnBattlefield(player1, "Sacred Mesa");
        assertThat(pegasusTokens()).isZero();
    }
}
