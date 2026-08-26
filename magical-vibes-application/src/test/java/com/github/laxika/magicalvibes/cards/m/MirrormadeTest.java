package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mirrormade.class, GloriousAnthem.class, GrizzlyBears.class, JayemdaeTome.class})
class MirrormadeTest extends BaseCardTest {

    @Test
    @DisplayName("Mirrormade copies an artifact")
    void copiesArtifact() {
        Permanent tome = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        castMirrormade();

        chooseCopy(tome);

        Permanent copy = findPermanent(player1, "Jayemdae Tome");
        assertThat(copy.getOriginalCard().getName()).isEqualTo("Mirrormade");
        assertThat(copy.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(copy.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Mirrormade copies an enchantment and its static effect")
    void copiesEnchantment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        castMirrormade();

        chooseCopy(anthem);

        Permanent copy = findPermanent(player1, "Glorious Anthem");
        assertThat(copy.getOriginalCard().getName()).isEqualTo("Mirrormade");
        assertThat(copy.getCard().getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mirrormade does not offer a copy choice without an artifact or enchantment")
    void doesNotOfferCopyChoiceWithoutValidPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMirrormade();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mirrormade");
    }

    private void castMirrormade() {
        harness.setHand(player1, List.of(new Mirrormade()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseCopy(Permanent target) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
    }
}
