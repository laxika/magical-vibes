package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryOfTheSleepless.class, DevilthornFox.class})
class EmissaryOfTheSleeplessTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a Spirit without morbid")
    void doesNotCreateSpiritWithoutMorbid() {
        castEmissary();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates a 1/1 white Spirit with flying when morbid is met")
    void createsSpiritWithMorbid() {
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        castEmissary();
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Creates a Spirit even if Emissary dies before its trigger resolves")
    void createsSpiritAfterSourceDies() {
        killFox(player2);
        castEmissary();
        findPermanent(player1, "Emissary of the Sleepless").setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        harness.assertInGraveyard(player1, "Emissary of the Sleepless");
    }

    @Test
    @DisplayName("An opponent's creature death enables the trigger")
    void opponentCreatureDeathEnablesTrigger() {
        killFox(player2);
        castEmissary();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A controller's creature death enables the trigger")
    void ownCreatureDeathEnablesTrigger() {
        killFox(player1);
        castEmissary();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("A death after Emissary enters does not retroactively trigger its ability")
    void laterDeathDoesNotTriggerAbility() {
        castEmissary();
        killFox(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A creature card already in a graveyard does not satisfy morbid")
    void graveyardCreatureAloneDoesNotEnableTrigger() {
        harness.setGraveyard(player2, java.util.List.of(new DevilthornFox()));
        castEmissary();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void killFox(Player player) {
        harness.addToBattlefield(player, new DevilthornFox());
        findPermanent(player, "Devilthorn Fox").setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player, "Devilthorn Fox");
    }

    private void castEmissary() {
        harness.castFromHand(player1, new EmissaryOfTheSleepless(), "{4}{W}");
        harness.passBothPriorities();
    }
}
