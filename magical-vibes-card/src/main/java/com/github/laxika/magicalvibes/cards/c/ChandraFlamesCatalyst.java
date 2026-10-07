package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastSpellsFromHandWithoutPayingManaCostUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1420")
@CardRegistration(set = "M21", collectorNumber = "332")
public class ChandraFlamesCatalyst extends Card {

    public ChandraFlamesCatalyst() {
        // +1: Chandra deals 3 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                "+1: Chandra deals 3 damage to each opponent."
        ));

        // −2: You may cast target red instant or sorcery card from your graveyard. If that
        // spell would be put into your graveyard, exile it instead.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, true,
                        new CardColorPredicate(CardColor.RED))),
                "−2: You may cast target red instant or sorcery card from your graveyard. "
                        + "If that spell would be put into your graveyard, exile it instead."
        ));

        // −8: Discard your hand, then draw seven cards. Until end of turn, you may cast spells from
        // your hand without paying their mana costs.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new DiscardHandEffect(),
                        new DrawCardEffect(7),
                        new AllowCastSpellsFromHandWithoutPayingManaCostUntilEndOfTurnEffect()
                ),
                "−8: Discard your hand, then draw seven cards. Until end of turn, you may cast spells "
                        + "from your hand without paying their mana costs."
        ));
    }
}
