package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeirdingShaman.class, PricklyBoggart.class})
class WeirdingShamanTest extends BaseCardTest {

    // ===== {3}{B}, Sacrifice a Goblin: Create two 1/1 black Goblin Rogue tokens =====

    @Test
    @DisplayName("Sacrificing a Goblin (itself) creates two 1/1 black Goblin Rogue tokens")
    void sacrificeGoblinCreatesTwoTokens() {
        addCreatureReady(player1, new WeirdingShaman());
        harness.addMana(player1, ManaColor.BLACK, 4);

        // Shaman is the only Goblin → auto-sacrifices itself to pay the cost
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin Rogue")).hasSize(2);
        harness.assertInGraveyard(player1, "Weirding Shaman");
    }

    @Test
    @DisplayName("Goblin Rogue tokens are 1/1 black")
    void tokensAreOneOneBlack() {
        addCreatureReady(player1, new WeirdingShaman());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Goblin Rogue");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.GOBLIN, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Ability requires {3}{B} to activate")
    void abilityRequiresMana() {
        addCreatureReady(player1, new WeirdingShaman());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice a Goblin can choose another Goblin instead of the Shaman")
    void canSacrificeAnotherGoblin() {
        Permanent shaman = addCreatureReady(player1, new WeirdingShaman());
        Permanent otherGoblin = addCreatureReady(player1, new PricklyBoggart());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, otherGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shaman);
        assertThat(findPermanents(player1, "Goblin Rogue")).hasSize(2);
        harness.assertInGraveyard(player1, "Prickly Boggart");
        harness.assertNotInGraveyard(player1, "Weirding Shaman");
    }
}
