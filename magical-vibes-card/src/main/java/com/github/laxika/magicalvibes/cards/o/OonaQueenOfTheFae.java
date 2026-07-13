package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "172")
public class OonaQueenOfTheFae extends Card {

    public OonaQueenOfTheFae() {
        // 1/1 blue and black Faerie Rogue creature token with flying, created per chosen-colour card.
        CreateTokenEffect faerieRogue = new CreateTokenEffect(
                1, "Faerie Rogue", 1, 1, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.BLACK),
                List.of(CardSubtype.FAERIE, CardSubtype.ROGUE),
                Set.of(Keyword.FLYING), Set.of());

        // {X}{U/B}: Choose a color. Target opponent exiles the top X cards of their library.
        // For each card of the chosen color exiled this way, create a 1/1 blue and black Faerie
        // Rogue token with flying.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{U/B}",
                List.of(new ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect(new XValue(), faerieRogue)),
                "{X}{U/B}: Choose a color. Target opponent exiles the top X cards of their library. "
                        + "For each card of the chosen color exiled this way, create a 1/1 blue and black "
                        + "Faerie Rogue creature token with flying.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
