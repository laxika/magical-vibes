package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FarbogExplorer;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandersAuthority.class, FarbogExplorer.class})
class CommandersAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Commander's Authority attaches it to the target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreature(player1);

        harness.setHand(player1, List.of(new CommandersAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Commander's Authority")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature's controller creates a Human token at their upkeep")
    void createsHumanTokenAtEnchantedControllerUpkeep() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(1);
        assertThat(humanTokens(player2)).isZero();
    }

    @Test
    @DisplayName("Token goes to the enchanted creature's controller, not the Aura's controller")
    void tokenGoesToEnchantedControllerNotAuraController() {
        Permanent creature = addCreature(player2);
        attachAuthority(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isZero();
        assertThat(humanTokens(player2)).isZero();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(humanTokens(player2)).isEqualTo(1);
        assertThat(humanTokens(player1)).isZero();
    }

    @Test
    @DisplayName("A token is created each upkeep")
    void tokensAccumulateOverUpkeeps() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("The granted upkeep ability belongs to the enchanted creature and its controller")
    void grantedAbilityHasCreatureSourceAndController() {
        Permanent creature = addCreature(player2);
        attachAuthority(creature);

        advanceToUpkeep(player2);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getSourcePermanentId()).isEqualTo(creature.getId());
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
        });
        harness.passBothPriorities();
        assertThat(humanTokens(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep ability does not target a player")
    void upkeepAbilityDoesNotTarget() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);

        advanceToUpkeep(player1);

        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.isSingleTarget()).isFalse());
        harness.passBothPriorities();
        assertThat(humanTokens(player1)).isEqualTo(1);
    }

    @Test
    @CardUsed(WitchbaneOrb.class)
    @DisplayName("A creature controlled by a player with hexproof still creates the token")
    void hexproofDoesNotPreventCreatingToken() {
        Permanent creature = addCreature(player2);
        attachAuthority(creature);
        harness.addToBattlefield(player2, new WitchbaneOrb());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(humanTokens(player2)).isEqualTo(1);
        assertThat(humanTokens(player1)).isZero();
    }

    @Test
    @CardUsed(Lignify.class)
    @DisplayName("A later Lignify removes the granted upkeep ability")
    void losingAllAbilitiesRemovesUpkeepTrigger() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isZero();
    }

    @Test
    @DisplayName("The created token is a 1/1 white Human creature")
    void createsTokenWithCorrectCharacteristics() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the Aura after the ability triggers does not stop the token")
    void triggeredAbilitySurvivesAuraRemoval() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof CommandersAuthority);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Authorities grant two separate upkeep abilities")
    void multipleAuthoritiesCreateMultipleTokens() {
        Permanent creature = addCreature(player1);
        attachAuthority(creature);
        attachAuthority(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(2);
    }

    private void attachAuthority(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CommandersAuthority());
        aura.setAttachedTo(creature.getId());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FarbogExplorer());
    }

    private long humanTokens(Player player) {
        return countPermanents(player, "Human");
    }
}
