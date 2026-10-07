package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StitchersApprentice.class, WalkingCorpse.class, VictimOfNight.class})
class StitchersApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability creates a 2/2 blue Homunculus token and then controller sacrifices a creature")
    void createsTokenAndSacrificesCreature() {
        Permanent apprentice = addCreatureReady(player1, new StitchersApprentice());
        harness.addToBattlefield(player1, new WalkingCorpse());
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

        // Controller must sacrifice a creature — with 3 creatures (apprentice tapped + corpse + token),
        // the player is prompted to choose
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Walking Corpse"));

        // Corpse should be gone, apprentice and token remain
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Stitcher's Apprentice");
        assertThat(countPermanents(player1, "Homunculus")).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller can sacrifice the newly created token")
    void canSacrificeNewlyCreatedToken() {
        Permanent apprentice = addCreatureReady(player1, new StitchersApprentice());
        harness.addToBattlefield(player1, new WalkingCorpse());
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
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Controller can sacrifice Stitcher's Apprentice itself")
    void canSacrificeApprenticeItself() {
        Permanent apprentice = addCreatureReady(player1, new StitchersApprentice());
        int apprenticeIdx = gd.playerBattlefields.get(player1.getId()).indexOf(apprentice);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, apprenticeIdx, null, null);
        harness.passBothPriorities();

        // Two creatures exist (tapped apprentice + new token) — player is prompted to choose
        // Choose to sacrifice the apprentice itself
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Stitcher's Apprentice"));

        // Apprentice is gone, token remains
        harness.assertNotOnBattlefield(player1, "Stitcher's Apprentice");
        assertThat(countPermanents(player1, "Homunculus")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new StitchersApprentice());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new StitchersApprentice());

        harness.addMana(player1, ManaColor.BLUE, 1); // Only 1 mana, need 2

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation pays mana and taps the Apprentice before resolving")
    void paysCostsBeforeCreatingToken() {
        Permanent apprentice = addCreatureReady(player1, new StitchersApprentice());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(apprentice.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(countPermanents(player1, "Homunculus")).isZero();
        harness.assertOnBattlefield(player1, "Stitcher's Apprentice");
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, apprentice.getId());
        harness.assertInGraveyard(player1, "Stitcher's Apprentice");
        assertThat(countPermanents(player1, "Homunculus")).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability requires blue mana, not just two mana")
    void cannotActivateWithoutBlueMana() {
        Permanent apprentice = addCreatureReady(player1, new StitchersApprentice());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(apprentice.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Homunculus")).isZero();
    }

    @Test
    @DisplayName("The ability resolves after its source dies and sacrifices its only token")
    void resolvesAfterSourceIsDestroyed() {
        Permanent apprentice = addCreatureReady(player1, new StitchersApprentice());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, apprentice.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Stitcher's Apprentice");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Homunculus")).isZero();
        assertThat(gameLogContains("sacrifices Homunculus")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
