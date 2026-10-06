package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RambunctiousMutt.class, LeoninScimitar.class, GloriousAnthem.class, GrizzlyBears.class, Forest.class})
class RambunctiousMuttTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target artifact an opponent controls")
    void etbDestroysTargetArtifactOpponentControls() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rambunctious Mutt");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB destroys target enchantment an opponent controls")
    void etbDestroysTargetEnchantmentOpponentControls() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rambunctious Mutt");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target an artifact or enchantment controlled by its caster")
    void cannotTargetOwnArtifactOrEnchantment() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Leonin Scimitar");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("ETB leaves no ability on the stack when there is no legal target")
    void etbLeavesNoAbilityOnStackWithoutLegalTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rambunctious Mutt");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Can enter when the only artifact and enchantment are controlled by its caster")
    void entersWithOnlyOwnArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rambunctious Mutt");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target its controller's enchantment")
    void cannotTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Glorious Anthem");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Target survives if its control changes to the ability's controller before resolution")
    void targetBecomesIllegalAfterControlChange() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotInGraveyard(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Rambunctious Mutt");
    }

    @Test
    @DisplayName("ETB resolves independently after Rambunctious Mutt leaves the battlefield")
    void etbResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RambunctiousMutt()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent mutt = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(mutt);
        gd.playerGraveyards.get(player1.getId()).add(mutt.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rambunctious Mutt");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }
}
