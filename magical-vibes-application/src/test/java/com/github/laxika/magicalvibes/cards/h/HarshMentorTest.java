package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarshMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent activating a creature's non-mana ability takes 2 damage")
    void opponentCreatureNonManaAbilityDeals2Damage() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithNonManaAbility(player2, CardType.CREATURE);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent activating an artifact's non-mana ability takes 2 damage")
    void opponentArtifactNonManaAbilityDeals2Damage() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithNonManaAbility(player2, CardType.ARTIFACT);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A mana ability does not trigger Harsh Mentor")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithAbility(player2, CardType.CREATURE, new AwardManaEffect(ManaColor.GREEN));
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        // Mana ability resolves immediately without using the stack; no trigger, no damage.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An enchantment's non-mana ability does not trigger (not artifact/creature/land)")
    void enchantmentAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithNonManaAbility(player2, CardType.ENCHANTMENT);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        // The ability is on the stack, but no Harsh Mentor trigger was placed on top of it.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller activating their own creature's ability does not trigger Harsh Mentor")
    void controllerOwnAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        // Harsh Mentor is at index 0, the ability creature at index 1.
        addPermanentWithNonManaAbility(player1, CardType.CREATURE);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void addPermanentWithNonManaAbility(Player player, CardType type) {
        addPermanentWithAbility(player, type, new BoostSelfEffect(1, 0));
    }

    private void addPermanentWithAbility(Player player, CardType type, CardEffect effect) {
        Card card = new Card();
        card.setName("Ability Source");
        card.setType(type);
        card.addActivatedAbility(new ActivatedAbility(true, null, List.of(effect), "{T}: ability."));
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
