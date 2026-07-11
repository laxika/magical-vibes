package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemonmailHauberkTest extends BaseCardTest {

    // ===== Equip by sacrificing a creature =====

    @Test
    @DisplayName("Equip by sacrificing a creature attaches equipment to target creature")
    void equipBySacrificingCreature() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = addCreatureReady(player1, createTokenCreature("Goblin Token"));

        harness.activateAbility(player1, 0, null, bears.getId());
        // Choose the token as the creature to sacrifice
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        Permanent hauberk = findPermanent(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isEqualTo(bears.getId());

        // Token should have been sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Token"));

        // Bears should get +4/+2 (2/2 base + 4/2 = 6/4)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip moves equipment from one creature to another")
    void equipMovesEquipmentBetweenCreatures() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent token1 = addCreatureReady(player1, createTokenCreature("Token A"));
        Permanent token2 = addCreatureReady(player1, createTokenCreature("Token B"));

        // Equip to firstBear by sacrificing token1
        harness.activateAbility(player1, 0, null, firstBear.getId());
        harness.handlePermanentChosen(player1, token1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Demonmail Hauberk").getAttachedTo())
                .isEqualTo(firstBear.getId());

        // Add a second bear and equip to it by sacrificing token2
        Permanent secondBear = addCreatureReady(player1, createTokenCreature("Second Bear"));

        int hauberkIndex = findPermanentIndex(player1, "Demonmail Hauberk");
        harness.activateAbility(player1, hauberkIndex, null, secondBear.getId());
        harness.handlePermanentChosen(player1, token2.getId());
        harness.passBothPriorities();

        Permanent hauberk = findPermanent(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isEqualTo(secondBear.getId());

        // Second bear gets +4/+2
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondBear)).isEqualTo(3);

        // First bear no longer has bonus
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice the currently equipped creature to re-equip to another")
    void canSacrificeEquippedCreatureToReEquip() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = addCreatureReady(player1, createTokenCreature("Goblin Token"));
        Permanent secondBear = addCreatureReady(player1, createTokenCreature("Second Bear"));

        // Equip to firstBear by sacrificing token
        harness.activateAbility(player1, 0, null, firstBear.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        // Now equip to secondBear by sacrificing firstBear (the equipped creature)
        int hauberkIndex = findPermanentIndex(player1, "Demonmail Hauberk");
        harness.activateAbility(player1, hauberkIndex, null, secondBear.getId());
        harness.handlePermanentChosen(player1, firstBear.getId());
        harness.passBothPriorities();

        // First bear should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Equipment should be attached to secondBear
        Permanent hauberk = findPermanent(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isEqualTo(secondBear.getId());
    }

    // ===== Helpers =====

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private int findPermanentIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }
}
