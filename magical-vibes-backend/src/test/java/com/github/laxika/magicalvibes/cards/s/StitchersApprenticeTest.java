package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControllerSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StitchersApprenticeTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Activated ability has CreateTokenEffect and ControllerSacrificesCreatureEffect")
    void abilityHasCorrectEffects() {
        StitchersApprentice card = new StitchersApprentice();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{1}{U}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CreateTokenEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(ControllerSacrificesCreatureEffect.class);
    }

    // ===== Token creation + sacrifice when controller has another creature =====

    @Test
    @DisplayName("Ability creates a 2/2 blue Homunculus token and then controller sacrifices a creature")
    void createsTokenAndSacrificesCreature() {
        harness.addToBattlefield(player1, new StitchersApprentice());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent apprentice = findPermanent(player1, "Stitcher's Apprentice");
        apprentice.setSummoningSick(false);
        int apprenticeIdx = gd.playerBattlefields.get(player1.getId()).indexOf(apprentice);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, apprenticeIdx, null, null);
        harness.passBothPriorities();

        // Token was created — there should be a Homunculus token
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Homunculus");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HOMUNCULUS);

        // Controller must sacrifice a creature — with 3 creatures (apprentice tapped + bears + token),
        // the player is prompted to choose
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        // Bears should be gone, apprentice and token remain
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stitcher's Apprentice");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Homunculus"))
                .count()).isEqualTo(1);
    }

    // ===== Sacrifice the token itself =====

    @Test
    @DisplayName("Controller can sacrifice the newly created token")
    void canSacrificeNewlyCreatedToken() {
        harness.addToBattlefield(player1, new StitchersApprentice());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent apprentice = findPermanent(player1, "Stitcher's Apprentice");
        apprentice.setSummoningSick(false);
        int apprenticeIdx = gd.playerBattlefields.get(player1.getId()).indexOf(apprentice);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, apprenticeIdx, null, null);
        harness.passBothPriorities();

        // Choose to sacrifice the token
        UUID tokenId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow().getId();
        harness.handlePermanentChosen(player1, tokenId);

        // Token gone, both non-token creatures remain
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Stitcher's Apprentice");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    // ===== Controller can sacrifice the Apprentice itself (Ruling 1) =====

    @Test
    @DisplayName("Controller can sacrifice Stitcher's Apprentice itself")
    void canSacrificeApprenticeItself() {
        harness.addToBattlefield(player1, new StitchersApprentice());

        Permanent apprentice = findPermanent(player1, "Stitcher's Apprentice");
        apprentice.setSummoningSick(false);
        int apprenticeIdx = gd.playerBattlefields.get(player1.getId()).indexOf(apprentice);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, apprenticeIdx, null, null);
        harness.passBothPriorities();

        // Two creatures exist (tapped apprentice + new token) — player is prompted to choose
        // Choose to sacrifice the apprentice itself
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Stitcher's Apprentice"));

        // Apprentice is gone, token remains
        harness.assertNotOnBattlefield(player1, "Stitcher's Apprentice");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Homunculus"))
                .count()).isEqualTo(1);
    }

    // ===== Summoning sickness prevents activation =====

    @Test
    @DisplayName("Cannot activate ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new StitchersApprentice());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Insufficient mana prevents activation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new StitchersApprentice());
        Permanent apprentice = findPermanent(player1, "Stitcher's Apprentice");
        apprentice.setSummoningSick(false);

        harness.addMana(player1, ManaColor.BLUE, 1); // Only 1 mana, need 2

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
