package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromExileOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "184")
public class SentinelOfLostLore extends Card {

    public SentinelOfLostLore() {
        ReturnTargetCardFromExileToHandEffect returnToHand =
                new ReturnTargetCardFromExileToHandEffect(new CardHasAdventurePredicate(), true);
        PutTargetCardFromExileOnBottomOfOwnersLibraryEffect putOnBottom =
                new PutTargetCardFromExileOnBottomOfOwnersLibraryEffect(
                        new CardHasAdventurePredicate(), true);
        ExileGraveyardCardsEffect exileGraveyard =
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE);

        SpellTarget returnTarget = target(1, 1);
        SpellTarget putOnBottomTarget = target(1, 1);
        SpellTarget graveyardTarget = target(1, 1);
        registerEffectTargetIndex(returnToHand, returnTarget.getIndex());
        registerEffectTargetIndex(putOnBottom, putOnBottomTarget.getIndex());
        registerEffectTargetIndex(exileGraveyard, graveyardTarget.getIndex());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                ChooseOneEffect.oneOrMore(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Return target card you own in exile that has an Adventure to your hand.",
                                returnToHand),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put target card you don't own in exile that has an Adventure on the bottom of its owner's library.",
                                putOnBottom),
                        new ChooseOneEffect.ChooseOneOption(
                                "Exile target player's graveyard.", exileGraveyard)
                ))));
    }
}
