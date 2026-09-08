package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StormTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure when multiple creatures deal combat damage to a player")
    void createsOneTreasureForOneCombatDamageEvent() {
        addReadyStorm(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        resolveCombatDamage();

        assertThat(treasureCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms at the end step with exactly five artifacts")
    void transformsWithFiveArtifacts() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 5; i++) {
            addArtifact(player1);
        }

        resolveEndStep(player1);

        assertThat(storm.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at the end step with only four artifacts")
    void doesNotTransformWithFourArtifacts() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 4; i++) {
            addArtifact(player1);
        }

        resolveEndStep(player1);

        assertThat(storm.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Vault of Catlacan adds one mana of the chosen color")
    void vaultAddsChosenColor() {
        Permanent vault = addTransformedVault(player1);

        harness.activateAbility(player1, indexOf(player1, vault), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vault of Catlacan adds blue mana for each artifact you control")
    void vaultAddsBlueForArtifacts() {
        Permanent vault = addTransformedVault(player1);
        addArtifact(player1);
        addArtifact(player1);
        addArtifact(player1);

        harness.activateAbility(player1, indexOf(player1, vault), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    private Permanent addReadyStorm(Player player) {
        Permanent perm = new Permanent(new StormTheVault());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedVault(Player player) {
        StormTheVault card = new StormTheVault();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addReadyAttacker(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private Permanent addArtifact(Player player) {
        Card card = new Card();
        card.setName("Artifact");
        card.setType(CardType.ARTIFACT);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private long treasureCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
    }

    private void resolveCombatDamage() {
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
