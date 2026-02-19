package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AiDraftEngine {

    private final Map<CardColor, Integer> colorAffinity = new EnumMap<>(CardColor.class);
    private int totalPicks = 0;

    public int pickCard(List<Card> pack) {
        if (pack.isEmpty()) {
            return 0;
        }

        int bestIndex = 0;
        double bestScore = -1;

        for (int i = 0; i < pack.size(); i++) {
            double score = scoreCardForDraft(pack.get(i));
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        Card picked = pack.get(bestIndex);
        totalPicks++;

        // Update color affinity
        if (picked.getColor() != null) {
            colorAffinity.merge(picked.getColor(), 1, Integer::sum);
        }
        // Multi-color: check mana cost for additional color signals
        if (picked.getManaCost() != null) {
            for (CardColor color : CardColor.values()) {
                String code = color.getCode();
                if (picked.getManaCost().contains("{" + code + "}")) {
                    colorAffinity.merge(color, 1, Integer::sum);
                }
            }
        }

        return bestIndex;
    }

    private double scoreCardForDraft(Card card) {
        double score;

        if (card.getType() == CardType.CREATURE) {
            int mv = card.getManaValue();
            int power = card.getPower() != null ? card.getPower() : 0;
            int toughness = card.getToughness() != null ? card.getToughness() : 0;
            score = mv * 10.0 + (power + toughness) * 5.0;
            score += keywordBonus(card);
        } else if (card.getType() == CardType.INSTANT || card.getType() == CardType.SORCERY) {
            score = card.getManaValue() * 8.0 + 15;
        } else if (card.getType() == CardType.ENCHANTMENT) {
            score = card.getManaValue() * 7.0 + 10;
        } else if (card.getType() == CardType.ARTIFACT) {
            score = card.getManaValue() * 8.0 + 5;
        } else if (card.getType() == CardType.PLANESWALKER) {
            score = card.getManaValue() * 12.0 + 30;
        } else {
            // Land or other
            score = 5;
        }

        // Color commitment: after 4 picks, prefer on-color cards
        if (totalPicks >= 4 && !colorAffinity.isEmpty()) {
            CardColor cardColor = card.getColor();
            if (cardColor != null) {
                int affinity = colorAffinity.getOrDefault(cardColor, 0);
                if (affinity > 0) {
                    score += affinity * 8.0;
                } else {
                    score *= 0.4;
                }
            }
            // Colorless cards (artifacts) are always fine
        }

        // Mana curve bonus: prefer cards that fill 2-5 CMC slots
        int mv = card.getManaValue();
        if (mv >= 2 && mv <= 5) {
            score += 10;
        }

        return score;
    }

    private double keywordBonus(Card card) {
        double bonus = 0;
        for (Keyword kw : card.getKeywords()) {
            bonus += switch (kw) {
                case FLYING -> 15;
                case FIRST_STRIKE -> 10;
                case DOUBLE_STRIKE -> 20;
                case TRAMPLE -> 10;
                case LIFELINK -> 10;
                case VIGILANCE -> 5;
                case HASTE -> 8;
                case REACH -> 5;
                case FEAR -> 8;
                case INDESTRUCTIBLE -> 20;
                default -> 0;
            };
        }
        return bonus;
    }

    public DeckBuildResult buildDeck(List<Card> pool) {
        // Determine top 2 colors
        List<Map.Entry<CardColor, Integer>> sortedColors = new ArrayList<>(colorAffinity.entrySet());
        sortedColors.sort(Comparator.<Map.Entry<CardColor, Integer>, Integer>comparing(Map.Entry::getValue).reversed());

        CardColor color1 = sortedColors.size() > 0 ? sortedColors.get(0).getKey() : CardColor.WHITE;
        CardColor color2 = sortedColors.size() > 1 ? sortedColors.get(1).getKey() : CardColor.GREEN;

        // Filter pool to on-color + colorless cards
        List<IndexedCard> onColorCards = new ArrayList<>();
        for (int i = 0; i < pool.size(); i++) {
            Card card = pool.get(i);
            CardColor cardColor = card.getColor();
            if (cardColor == null || cardColor == color1 || cardColor == color2) {
                onColorCards.add(new IndexedCard(i, card, scoreCardForDraft(card)));
            }
        }

        // Sort by score descending
        onColorCards.sort(Comparator.comparingDouble(IndexedCard::score).reversed());

        // Take best 23 non-land cards
        List<Integer> cardIndices = new ArrayList<>();
        for (IndexedCard ic : onColorCards) {
            if (ic.card().getType() != CardType.LAND && cardIndices.size() < 23) {
                cardIndices.add(ic.index());
            }
        }

        // If we don't have 23 non-land cards, add off-color ones
        if (cardIndices.size() < 23) {
            for (int i = 0; i < pool.size(); i++) {
                if (!cardIndices.contains(i) && pool.get(i).getType() != CardType.LAND) {
                    cardIndices.add(i);
                    if (cardIndices.size() >= 23) break;
                }
            }
        }

        // Calculate basic lands proportional to mana symbols
        Map<String, Integer> basicLands = calculateBasicLands(pool, cardIndices, color1, color2);

        return new DeckBuildResult(cardIndices, basicLands);
    }

    private Map<String, Integer> calculateBasicLands(List<Card> pool, List<Integer> cardIndices,
                                                      CardColor color1, CardColor color2) {
        Map<ManaColor, Integer> symbolCount = new EnumMap<>(ManaColor.class);

        for (int idx : cardIndices) {
            Card card = pool.get(idx);
            if (card.getManaCost() == null) continue;
            String cost = card.getManaCost();
            for (ManaColor mc : ManaColor.values()) {
                if (mc == ManaColor.COLORLESS) continue;
                String symbol = "{" + mc.getCode() + "}";
                int pos = 0;
                while ((pos = cost.indexOf(symbol, pos)) != -1) {
                    symbolCount.merge(mc, 1, Integer::sum);
                    pos += symbol.length();
                }
            }
        }

        int totalSymbols = symbolCount.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Integer> lands = new HashMap<>();
        lands.put("Plains", 0);
        lands.put("Island", 0);
        lands.put("Swamp", 0);
        lands.put("Mountain", 0);
        lands.put("Forest", 0);

        if (totalSymbols == 0) {
            // Default split between 2 colors
            String land1 = colorToBasicLand(color1);
            String land2 = colorToBasicLand(color2);
            lands.put(land1, 9);
            lands.put(land2, 8);
        } else {
            int landsRemaining = 17;
            for (Map.Entry<ManaColor, Integer> entry : symbolCount.entrySet()) {
                String landName = manaColorToBasicLand(entry.getKey());
                int count = Math.round((float) entry.getValue() / totalSymbols * 17);
                count = Math.max(count, 1); // At least 1 if any symbols
                lands.put(landName, count);
                landsRemaining -= count;
            }
            // Adjust if over/under 17
            String primaryLand = colorToBasicLand(color1);
            lands.put(primaryLand, lands.get(primaryLand) + landsRemaining);
        }

        return lands;
    }

    private String colorToBasicLand(CardColor color) {
        return switch (color) {
            case WHITE -> "Plains";
            case BLUE -> "Island";
            case BLACK -> "Swamp";
            case RED -> "Mountain";
            case GREEN -> "Forest";
        };
    }

    private String manaColorToBasicLand(ManaColor color) {
        return switch (color) {
            case WHITE -> "Plains";
            case BLUE -> "Island";
            case BLACK -> "Swamp";
            case RED -> "Mountain";
            case GREEN -> "Forest";
            case COLORLESS -> "Plains"; // Should not happen
        };
    }

    public record DeckBuildResult(List<Integer> cardIndices, Map<String, Integer> basicLands) {}

    private record IndexedCard(int index, Card card, double score) {}
}
