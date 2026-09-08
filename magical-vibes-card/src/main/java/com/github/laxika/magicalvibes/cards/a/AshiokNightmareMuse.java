package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastUpToNSpellsFromOpponentsExileEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "208")
public class AshiokNightmareMuse extends Card {

    public AshiokNightmareMuse() {
        ExileTopCardsOfEachOpponentEffect nightmareTrigger =
                new ExileTopCardsOfEachOpponentEffect(2);
        CreateTokenEffect nightmareToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Nightmare", 2, 3,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.BLACK),
                List.of(CardSubtype.NIGHTMARE), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.ON_ATTACK, nightmareTrigger, EffectSlot.ON_BLOCK, nightmareTrigger),
                List.of(), false, false, false, 0, Set.of());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(nightmareToken),
                "+1: Create a 2/3 blue and black Nightmare creature token."));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ReturnTargetPermanentToHandThenEffect(
                        new TargetPlayerExilesFromHandEffect(1),
                        ThenEffectRecipient.TARGET_OWNER_AS_TARGET)),
                "\u22123: Return target nonland permanent to its owner's hand, then that player exiles a card from their hand.",
                TargetFilters.nonlandPermanent()));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CastUpToNSpellsFromOpponentsExileEffect(3)),
                "\u22127: You may cast up to three spells from among face-up cards your opponents own from exile without paying their mana costs."));
    }
}
