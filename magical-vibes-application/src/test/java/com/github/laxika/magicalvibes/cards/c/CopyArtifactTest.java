package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopyArtifact.class, JayemdaeTome.class, GrizzlyBears.class})
class CopyArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an artifact and remains an enchantment")
    void copiesArtifactAndRemainsEnchantment() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new CopyArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Copy Artifact"))
                .findFirst()
                .orElse(null);

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Jayemdae Tome");
        assertThat(copy.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(copy.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(copy.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Does not offer to copy a non-artifact")
    void doesNotOfferToCopyNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CopyArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Copy Artifact")
                        && permanent.getCard().getType().equals(CardType.ENCHANTMENT));
    }

    @Test
    @DisplayName("Enters as an enchantment when the copy is declined")
    void entersAsItselfWhenCopyIsDeclined() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new CopyArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Copy Artifact")
                        && permanent.getCard().getType().equals(CardType.ENCHANTMENT));
    }
}
