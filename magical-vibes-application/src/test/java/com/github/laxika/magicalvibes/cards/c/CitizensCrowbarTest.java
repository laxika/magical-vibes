package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CitizensCrowbar.class, GrizzlyBears.class, LeoninScimitar.class, GloriousAnthem.class})
class CitizensCrowbarTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Citizen's Crowbar creates and equips a Citizen token")
    void enteringCreatesAndEquipsCitizen() {
        harness.setHand(player1, List.of(new CitizensCrowbar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent crowbar = findPermanent(player1, "Citizen's Crowbar");
        Permanent citizen = findPermanent(player1, "Citizen");

        assertThat(crowbar.getAttachedTo()).isEqualTo(citizen.getId());
        assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(citizen.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature can sacrifice Citizen's Crowbar to destroy an artifact")
    void destroysTargetArtifact() {
        Permanent creature = addReadyCreature(player1);
        Permanent crowbar = addReadyCrowbar(player1);
        crowbar.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Citizen's Crowbar");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature can destroy an enchantment")
    void destroysTargetEnchantment() {
        Permanent creature = addReadyCreature(player1);
        Permanent crowbar = addReadyCrowbar(player1);
        crowbar.setAttachedTo(creature.getId());
        addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, gd.playerBattlefields.get(player2.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Granted ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent crowbar = addReadyCrowbar(player1);
        crowbar.setAttachedTo(creature.getId());
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCrowbar(Player player) {
        Permanent permanent = new Permanent(new CitizensCrowbar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent permanent = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
