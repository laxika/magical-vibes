package com.github.laxika.magicalvibes.service;
import com.github.laxika.magicalvibes.carddata.*;
import com.github.laxika.magicalvibes.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckValidationServiceTest {
    private final DeckLegalityRegistry registry = mock(DeckLegalityRegistry.class);
    private final DeckValidationService validator = new DeckValidationService(registry);
    DeckValidationServiceTest() {
        when(registry.status(any(), any())).thenReturn("legal");
        when(registry.snapshot(any())).thenReturn(LegalitySnapshot.empty());
    }
    private Card card(String name, CardType type) {
        Card card = new Card(); card.setName(name); card.setType(type); card.setManaCost("{1}"); return card;
    }
    private Card land() {
        Card land = card("Plains", CardType.LAND); land.setSupertypes(Set.of(CardSupertype.BASIC));
        land.setSubtypes(List.of(CardSubtype.PLAINS)); land.setColorIdentity(List.of(CardColor.WHITE)); return land;
    }
    private Card commander() {
        Card commander = card("Commander", CardType.CREATURE); commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setColorIdentity(List.of(CardColor.WHITE)); return commander;
    }
    @Test void acceptsCommanderAndNinetyNineBasicLands() {
        assertThat(validator.validate(new DeckDefinition(Collections.nCopies(99, land()), List.of(), commander()), DeckFormat.COMMANDER).valid()).isTrue();
    }
    @Test void rejectsWrongIdentityAndDuplicatesAcrossPrintings() {
        Card first = card("Blue Spell", CardType.INSTANT); first.setColorIdentity(List.of(CardColor.BLUE));
        Card reprint = card("Blue Spell", CardType.INSTANT); reprint.setColorIdentity(List.of(CardColor.BLUE));
        List<Card> main = new ArrayList<>(Collections.nCopies(97, land())); main.add(first); main.add(reprint);
        var errors = validator.validate(new DeckDefinition(main, List.of(), commander()), DeckFormat.COMMANDER).errors();
        assertThat(errors).anyMatch(error -> error.contains("maximum 1")).anyMatch(error -> error.contains("color identity"));
    }
    @Test void restrictedCopiesCountSideboard() {
        Card restricted = card("Restricted", CardType.ARTIFACT);
        when(registry.status(restricted, DeckFormat.VINTAGE)).thenReturn("restricted");
        List<Card> main = new ArrayList<>(Collections.nCopies(59, land())); main.add(restricted);
        assertThat(validator.validate(new DeckDefinition(main, List.of(restricted), null), DeckFormat.VINTAGE).errors())
                .anyMatch(error -> error.contains("maximum 1"));
    }
    @Test void unknownLegalityNeverPassesRestrictedFormat() {
        when(registry.status(any(), eq(DeckFormat.MODERN))).thenReturn("unknown");
        assertThat(validator.validate(new DeckDefinition(Collections.nCopies(60, land()), List.of(), null), DeckFormat.MODERN).valid()).isFalse();
    }
    @Test void unlimitedCopyExceptionDoesNotOverrideColorIdentity() {
        Card rats = card("Rats", CardType.CREATURE); rats.setCardText("A deck can have any number of cards named Rats.");
        rats.setColorIdentity(List.of(CardColor.BLACK));
        var errors = validator.validate(new DeckDefinition(Collections.nCopies(99, rats), List.of(), commander()), DeckFormat.COMMANDER).errors();
        assertThat(errors).noneMatch(error -> error.contains("maximum")).anyMatch(error -> error.contains("color identity"));
    }
    @Test void standardChecksBothDeckAndSideboardSize() {
        var errors = validator.validate(new DeckDefinition(Collections.nCopies(59, land()), Collections.nCopies(16, land()), null), DeckFormat.STANDARD).errors();
        assertThat(errors).anyMatch(error -> error.contains("60")).anyMatch(error -> error.contains("15"));
    }

    @Test void recognizesLegendaryCreaturesDefinedOutsideTheBattlefield() {
        Card grist = card("Grist", CardType.PLANESWALKER);
        grist.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        grist.addEffect(EffectSlot.STATIC, new com.github.laxika.magicalvibes.model.effect.BecomeCreatureOutsideBattlefieldEffect(
                1, 1, List.of(CardSubtype.INSECT)));
        assertThat(validator.eligibleCommander(grist)).isTrue();
    }
}
