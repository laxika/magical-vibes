package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GallifreyCouncilChamber.class})
class GallifreyCouncilChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and surveils one")
    void entersAndSurveilsOne() {
        GameData gd = harness.getGameData();
        Card topCard = new GallifreyCouncilChamber();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GallifreyCouncilChamber()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana casts either a Time Lord or Alien spell")
    void restrictedManaCastsEitherListedSubtypeSpell() {
        addReadyLand();
        produceRestrictedMana();

        harness.setHand(player1, List.of(createSpell("Time Lord spell", CardSubtype.TIME_LORD)));
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        produceRestrictedMana();
        harness.setHand(player1, List.of(createSpell("Alien spell", CardSubtype.ALIEN)));
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast an unrelated subtype spell")
    void restrictedManaRejectsUnlistedSubtypeSpell() {
        addReadyLand();
        produceRestrictedMana();
        harness.setHand(player1, List.of(createSpell("Human spell", CardSubtype.HUMAN)));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana activates an ability of a listed subtype source")
    void restrictedManaActivatesListedSubtypeAbility() {
        addReadyLand();
        harness.addToBattlefield(player1, createCreatureWithLifeAbility("Alien ability", CardSubtype.ALIEN));
        produceRestrictedMana();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private Permanent addReadyLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GallifreyCouncilChamber());
        land.setSummoningSick(false);
        return land;
    }

    private void produceRestrictedMana() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
    }

    private static Card createSpell(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{U}");
        card.setColor(CardColor.BLUE);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private static Card createCreatureWithLifeAbility(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        card.addActivatedAbility(new ActivatedAbility(
                false, "{U}", List.of(new GainLifeEffect(3)), "{U}: You gain 3 life."));
        return card;
    }
}
