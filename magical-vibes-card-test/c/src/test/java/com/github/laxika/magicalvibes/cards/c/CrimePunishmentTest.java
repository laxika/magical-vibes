package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BloodCrypt;
import com.github.laxika.magicalvibes.cards.m.MagewrightsStone;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.r.RainOfGore;
import com.github.laxika.magicalvibes.cards.s.SkullmeadCauldron;
import com.github.laxika.magicalvibes.cards.w.WritOfPassage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimePunishment.class, MistralCharger.class, RainOfGore.class, MagewrightsStone.class,
        SkullmeadCauldron.class, BloodCrypt.class, WritOfPassage.class})
class CrimePunishmentTest extends BaseCardTest {

    private static final int CRIME = 0;
    private static final int PUNISHMENT = 1;

    @Test
    @DisplayName("Crime puts a creature from an opponent's graveyard onto the battlefield under your control")
    void crimeReturnsCreatureFromOpponentGraveyard() {
        Card creature = new MistralCharger();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        harness.castSorcery(player1, 0, CRIME, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistral Charger");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Crime can put an enchantment from an opponent's graveyard onto the battlefield")
    void crimeReturnsEnchantmentFromOpponentGraveyard() {
        Card enchantment = new RainOfGore();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        harness.castSorcery(player1, 0, CRIME, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(enchantment.getId());
    }

    @Test
    @DisplayName("Crime cannot target a card that is neither a creature nor an enchantment")
    void crimeCannotTargetArtifactCard() {
        Card artifact = new MagewrightsStone();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, CRIME, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crime cannot target a card in your own graveyard")
    void crimeCannotTargetOwnGraveyard() {
        Card creature = new MistralCharger();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, CRIME, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Punishment destroys matching artifacts, creatures, and enchantments and leaves other permanents")
    void punishmentDestroysMatchingPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MagewrightsStone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RainOfGore());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new SkullmeadCauldron());
        Permanent differentType = harness.addToBattlefieldAndReturn(player2, new BloodCrypt());

        harness.setHand(player1, List.of(new CrimePunishment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{PUNISHMENT}, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .doesNotContain(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(artifact.getId(), enchantment.getId())
                .contains(differentManaValue.getId(), differentType.getId());
    }

    @Test
    @DisplayName("Crime returns an Aura attached to the only legal creature")
    void crimeReturnsAuraAttachedToLegalCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Card aura = new WritOfPassage();
        harness.setGraveyard(player2, List.of(aura));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        harness.castSorcery(player1, 0, CRIME, aura.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, host.getId());

        harness.assertOnBattlefield(player1, "Writ of Passage");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(aura.getId()))
                .singleElement()
                .satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(host.getId()));
        harness.assertNotInGraveyard(player2, "Writ of Passage");
    }

    @Test
    @DisplayName("Crime does not return a target that left the graveyard before resolution")
    void crimeDoesNotReturnMissingTarget() {
        Card creature = new MistralCharger();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        harness.castSorcery(player1, 0, CRIME, creature.getId());
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mistral Charger");
        harness.assertInGraveyard(player1, "Crime // Punishment");
    }

    @Test
    @DisplayName("Punishment with X zero leaves lands and positive mana value permanents")
    void punishmentForZeroLeavesLandsAndPositiveManaValues() {
        harness.addToBattlefield(player1, new BloodCrypt());
        harness.addToBattlefield(player2, new MistralCharger());
        harness.setHand(player1, List.of(new CrimePunishment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{PUNISHMENT}, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blood Crypt");
        harness.assertOnBattlefield(player2, "Mistral Charger");
        harness.assertInGraveyard(player1, "Crime // Punishment");
    }
    private void addCrimeMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
