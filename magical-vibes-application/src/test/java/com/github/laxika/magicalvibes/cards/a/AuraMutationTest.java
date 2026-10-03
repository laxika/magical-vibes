package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KavuLair;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraMutation.class, KavuLair.class, AncientKavu.class})
class AuraMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the enchantment and creates Saprolings equal to its mana value")
    void destroysEnchantmentAndCreatesSaprolings() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuLair());
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Kavu Lair");
        harness.assertInGraveyard(player2, "Kavu Lair");
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(3)
                .allSatisfy(saproling -> {
                    assertThat(saproling.getCard().isToken()).isTrue();
                    assertThat(saproling.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(saproling.getCard().getPower()).isEqualTo(1);
                    assertThat(saproling.getCard().getToughness()).isEqualTo(1);
                    assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    @DisplayName("Creates Saprolings even when the enchantment is indestructible")
    void createsSaprolingsWhenEnchantmentIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuLair());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Kavu Lair");
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    @Test
    @DisplayName("Can destroy your own enchantment and create Saprolings")
    void canTargetOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KavuLair());
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Kavu Lair");
        harness.assertInGraveyard(player1, "Kavu Lair");
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Creates Saprolings even when the enchantment regenerates")
    void createsSaprolingsWhenEnchantmentRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuLair());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Kavu Lair");
        harness.assertNotInGraveyard(player2, "Kavu Lair");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    @Test
    @DisplayName("Creates no Saprolings when the target leaves before resolution")
    void createsNoSaprolingsWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuLair());
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.setHand(player2, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kavu Lair");
        harness.assertInGraveyard(player1, "Aura Mutation");
        harness.assertInGraveyard(player2, "Aura Mutation");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
